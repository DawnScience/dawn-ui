package org.dawnsci.mapping.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.HighAspectImageDisplay;
import org.dawnsci.mapping.ui.datamodel.IMapFileEventListener;
import org.dawnsci.mapping.ui.datamodel.IMapPlotController;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject.IAxisMoveListener;
import org.dawnsci.mapping.ui.datamodel.MapObject;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.dawnsci.mapping.ui.datamodel.ReMappedData;
import org.dawnsci.mapping.ui.datamodel.VectorMapData;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceViewIterator;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IVectorTrace;
import org.eclipse.dawnsci.plotting.api.trace.IVectorTrace.ArrowConfiguration;
import org.eclipse.dawnsci.plotting.api.trace.IVectorTrace.VectorNormalization;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicShape;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapPlotManager implements IMapPlotController{
	
	private IMapFileController fileManager;
	private IPlottingService plotService;
	private EventAdmin eventAdmin;
	
	private IPlottingSystem<Composite> map;
	private IPlottingSystem<Composite> data;
	private ConcurrentLinkedDeque<MapTrace> layers;
	private ExecutorService executor;
	private ExecutorService detectorExecutor;
	private AtomicReference<Runnable> atomicRunnable = new AtomicReference<>();
	private AtomicReference<Future<?>> atomicFuture = new AtomicReference<>();
	private volatile Dataset merge;
	private AtomicInteger atomicPosition;
	private boolean firstHold = true;
	private HighAspectImageDisplay highAspectImageDisplayMode = HighAspectImageDisplay.IMAGE;
	private IAxisMoveListener moveListener;
	
	private static final int SMALLFIRST = 25;
	private static final int LARGESECOND = 100;
	private static final int POINTSIZE = 10;
	
	private static final Logger logger = LoggerFactory.getLogger(MapPlotManager.class);
	
	public void setMapFileController(IMapFileController fileManager) {
		this.fileManager = fileManager;
	}
	
	public void setPlotService(IPlottingService service) {
		plotService = service;
	}
	
	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
	
	public MapPlotManager() {
		
		executor = Executors.newSingleThreadExecutor();
		detectorExecutor = Executors.newSingleThreadExecutor();
		
		atomicPosition = new AtomicInteger(0);
		layers = new ConcurrentLinkedDeque<MapTrace>();
		
		moveListener = new IAxisMoveListener() {
			
			@Override
			public void axisMoved() {
				if (Display.getCurrent() == null) {
					Display.getDefault().syncExec(() -> axisMoved());
					return;
				}
				
				for (MapTrace t : layers) {
					if (t.getMap() instanceof LiveStreamMapObject) {
						LiveStreamMapObject l = (LiveStreamMapObject)t.getMap();
						List<IDataset> axes = l.getAxes();
						double[] range = l.getRange();
						ITrace trace = t.getTrace();
						if (trace != null && trace instanceof IImageTrace) {
							IImageTrace im = (IImageTrace)trace;
							im.setGlobalRange(range);
							im.setAxes(axes, false);
						}
					}
				}
			}
		};
		
	}
	
	public void init() {
		fileManager.addListener(new IMapFileEventListener() {
			
			@Override
			public void mapFileStateChanged(MappedDataFile file) {
				updatePlot();
			}
		});
	}
	
	private IPlottingSystem<?> getMapPlot() {
		if (map == null || map.isDisposed()) {
			map = plotService.getPlottingSystem("Map");
			initMapPlot(map);
		}
		return map;
	}
	
	private void initMapPlot(IPlottingSystem<?> system) {
		system.addClickListener(new IClickListener() {
			
			@Override
			public void doubleClickPerformed(ClickEvent evt) {
				//No double click action
			}
			
			@Override
			public void clickPerformed(ClickEvent evt) {
					plotData(evt.getxValue(), evt.getyValue(),evt.isShiftDown());
			}
			
		});
		
		system.addClickListener(new IClickListener() {
			
			@Override
			public void doubleClickPerformed(final ClickEvent evt) {
				sendEvent(evt,true);
			}
			
			@Override
			public void clickPerformed(final ClickEvent evt) {
				sendEvent(evt,false);
			}
			
			private void sendEvent(final ClickEvent evt, boolean isDoubleClick) {
				Map<String,Object> props = new HashMap<>();
				PlottableMapObject topMap = getTopMap();
				String path = topMap == null ? null : topMap.getPath();
				MappedDataFile p = fileManager.getArea().getParentFile(topMap);
				if (p != null && p.getParentPath() != null) {
					path = p.getParentPath();
				}
				props.put("event", new MapClickEvent(evt, isDoubleClick, path));
		
				eventAdmin.postEvent(new Event(EVENT_TOPIC_MAPVIEW_CLICK, props));
			}
		});
	}
	
	private IPlottingSystem<?> getDataPlot() {
		if (data == null || data.isDisposed()) {
			data = plotService.getPlottingSystem("Detector Data");
			initDataPlot(data);
		}
		return data;
	}
	
	private void initDataPlot(IPlottingSystem<?> dataPlot) {
		if (dataPlot.getAxes() != null) {
			List<IAxis> axes = dataPlot.getAxes();
			for (IAxis axis : axes) axis.setAxisAutoscaleTight(true);
		}
	}
	
	public void updatePlot(){	
		Runnable r = atomicRunnable.getAndSet(new Runnable() {
			
			@Override
			public void run() {
				updateState();
				
			}
		});
		
		if (r == null) {
			Future<?> f = executor.submit(()->{
				Runnable run = atomicRunnable.getAndSet(null);
				if (run == null) return;
				run.run();
			});
			
			atomicFuture.set(f);
		}
		
	}
	
	public void plotData(final double x, final double y, boolean hold) {
		if (!plotsReady()) return;
		IPlottingSystem<?> dataPlot = getDataPlot();
		IPlottingSystem<?> mapPlot = getMapPlot();
		
		final PlottableMapObject topMap = getTopMap(x,y);
		if (topMap == null)  {
			if (!hold) dataPlot.clear();
			return;
		}
		
		if (hold) {
			plotDataWithHold(x, y, topMap, dataPlot, mapPlot);
		} else {
			plotData(x, y, topMap, dataPlot, mapPlot);
		}
	}
	
	private void plotData(double x, double y, final PlottableMapObject topMap, IPlottingSystem<?> dataPlot, IPlottingSystem<?> mapPlot) {
		merge = null;
		firstHold = true;
		atomicPosition.set(0);
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					long t = System.currentTimeMillis();
					IDataset s = topMap.getSpectrum(x,y);
					if (s == null) {
						dataPlot.clear();
						return;
					}
					logger.info("Slice time {} ms for shape {} of {}", (System.currentTimeMillis()-t), Arrays.toString(s.getShape()), topMap.toString());
					if (s.getSize() == 1) return;
					
					int[] ss = ShapeUtils.squeezeShape(s.getShape(), false);
					
					ITrace[] traces = null;
					
					if (ss.length == 2 && isHighAspectImage(ss)) {
						traces = buildTraces(dataPlot, s, highAspectImageDisplayMode);
					} else {
						traces = buildTraces(dataPlot, s, null);
					}

					if (traces == null || traces.length == 0) return;
					
					final ITrace[] finalTraces = traces;
					
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							
							dataPlot.clear();
							for (ITrace tr : finalTraces) {
								if (tr == null) continue;
								dataPlot.addTrace(tr);
							}
							
							dataPlot.repaint();
							
							Collection<IRegion> regions = mapPlot.getRegions();
							Iterator<IRegion> it = regions.iterator();
							while (it.hasNext()) {
								IRegion r = it.next();
								if (r.getUserObject() ==  MapPlotManager.this){
									mapPlot.removeRegion(r);
								}
							}
							mapPlot.repaint(false);
						}

					});
				} catch (Exception e) {
					logger.error("Error plotting spectrum");
				}

			}
		};
		
		detectorExecutor.submit(r);
	}
	
	private ITrace[] buildTraces(IPlottingSystem<?> dataPlot, IDataset d, HighAspectImageDisplay display) {
		
		if (display == null || display == HighAspectImageDisplay.IMAGE) {
			d.squeeze();
			
			ITrace t = null;
			
			if (d.getRank() == 1) {
				t = MetadataPlotUtils.buildLineTrace(d, dataPlot);
			} else {
				t = MetadataPlotUtils.buildTrace(d, dataPlot);
			}
			
			return new ITrace[] {t};
		}
		
		switch (highAspectImageDisplayMode) {

		case LINES:
			return buildLineTracesFromImage(d, dataPlot);
		case SUM:
			return new ITrace[] {buildLineTraceFromImageSum(d, dataPlot)};
		default:
			return new ITrace[0];
		}
		
	}
	
	@Override
	public HighAspectImageDisplay getHighAspectImageDisplayMode() {
		return highAspectImageDisplayMode;
	}

	@Override
	public void setHighAspectImageDisplayMode(HighAspectImageDisplay highAspectImageDisplayMode) {
		this.highAspectImageDisplayMode = highAspectImageDisplayMode;
	}

	private boolean isHighAspectImage(int[] shape) {
		return  shape[0] < SMALLFIRST && shape[1] > LARGESECOND;
	}
	
	private void plotDataWithHold(final double x, final double y, final PlottableMapObject topMap, IPlottingSystem<?> dataPlot, IPlottingSystem<?> mapPlot)  {
		
		if (topMap == null) return;
		final ILazyDataset lz = topMap.getSpectrum(x,y);
		
		Runnable r = null;
		
		int[] ss = ShapeUtils.squeezeShape(lz.getShape(), false);
		
		boolean isHighAspect = isHighAspectImage(ss);
		
		if (ss.length == 2 && (!isHighAspect || (isHighAspect && highAspectImageDisplayMode.equals(HighAspectImageDisplay.IMAGE)))) {
			
			r = new Runnable() {
				
				@Override
				public void run() {
					if (firstHold) dataPlot.clear();
					firstHold = false;
					IDataset s = null;
					try {
						s = DatasetUtils.sliceAndConvertLazyDataset(lz).squeeze();
					} catch (DatasetException e) {
						logger.error("Could not get data from lazy dataset", e);
						return;
					}
					if (s != null) {
						Dataset mergedDataset = getMergedDataset(s);
						int pos = atomicPosition.getAndIncrement() % 4;
						int[] mShape = mergedDataset.getShape();
						SliceND slice = new SliceND(mShape);
						if (pos == 0){
							slice.setSlice(0,0, mShape[0]/2, 1);
							slice.setSlice(1,0, mShape[1]/2, 1);
							mergedDataset.setSlice(s,slice);

						} else if (pos == 1) {

								slice.setSlice(0,0, mShape[0]/2, 1);
								slice.setSlice(1,mShape[1]/2, mShape[1], 1);
								mergedDataset.setSlice(s,slice);

						} else if (pos == 2) {

							slice.setSlice(0,mShape[0]/2, mShape[0], 1);
							slice.setSlice(1,0,mShape[1]/2, 1);
							mergedDataset.setSlice(s,slice);

					} else if (pos == 3) {

						slice.setSlice(0,mShape[0]/2, mShape[0], 1);
						slice.setSlice(1,mShape[1]/2, mShape[1], 1);
						mergedDataset.setSlice(s,slice);

					}
						MetadataPlotUtils.plotDataWithMetadata(mergedDataset, dataPlot);
								
						
					}
				}
			};
			
			
			
		} else {
			r = new Runnable() {
				
				@Override
				public void run() {
					IDataset s = null;
					try {
						s = DatasetUtils.sliceAndConvertLazyDataset(lz);
					} catch (DatasetException e) {
						logger.error("Could not get data from lazy dataset", e);
						return;
					}
					if (s != null) {
						final ITrace[] traces = buildTraces(dataPlot,s, isHighAspect ? highAspectImageDisplayMode : null);
						
						if (traces == null || traces.length == 0) return;
						
						Display.getDefault().syncExec(new Runnable() {
							
							@Override
							public void run() {
								if (firstHold) dataPlot.clear();
								firstHold = false;
								
								for (ITrace t : traces) {
									dataPlot.addTrace(t);
								}
								
								
								try {
									String uniqueName = RegionUtils.getUniqueName("Click", mapPlot);
									final IRegion re = mapPlot.createRegion(uniqueName, RegionType.POINT);
									re.setROI(new PointROI(x, y));
									re.setAlpha(255);
									if (traces[0] instanceof ILineTrace) {
										re.setRegionColor(((ILineTrace)traces[0]).getTraceColor());
									}
									re.setUserObject(MapPlotManager.this);
									re.addROIListener(new IROIListener.Stub() {
										@Override
										public void roiSelected(ROIEvent evt) {
											Collection<ITrace> ts = dataPlot.getTraces();
											for (ITrace t : ts) if (t instanceof ILineTrace) ((ILineTrace)t).setLineWidth(1);
											for (ITrace t : traces) if (t instanceof ILineTrace) ((ILineTrace)t).setLineWidth(2);

										}
									});
//									((AbstractSelectionRegion)re).setHighlighted(true);
									mapPlot.addRegion(re);
									re.setMobile(false);
									re.setUserRegion(false);
									re.setUserObject(MapPlotManager.this);
									mapPlot.clearRegionTool();
									dataPlot.repaint();
								} catch (Exception e) {
									logger.error("Error plotting line trace",e);
								}
								
								
							}
						});
					}
					
				}
			};
		}
		
		detectorExecutor.execute(r);
		
	}
	
	private boolean plotsReady() {
		IPlottingSystem<?> dataPlot = getDataPlot();
		IPlottingSystem<?> mapPlot = getMapPlot();
		return dataPlot != null && mapPlot != null;
	}
	

	private void updateState() {
		if (!plotsReady()) return;
		
		IPlottingSystem<?> dataPlot = getDataPlot();
		IPlottingSystem<?> mapPlot = getMapPlot();
		
		List<PlottableMapObject> plottedObjects = fileManager.getPlottedObjects();
		
		if (plottedObjects.isEmpty()) {
			layers.clear();
			mapPlot.clear();
			dataPlot.clear();
		}
		
		Iterator<MapTrace> it = layers.iterator();
		
		List<MapTrace> stale = new ArrayList<>();
		
		while (it.hasNext()) {
			MapTrace m = it.next();
			if (m.getTrace() instanceof IPaletteTrace) {
				IPaletteTrace t = (IPaletteTrace) m.getTrace();

				if (!t.isRescaleHistogram()) {
					m.getMap().setColorRange(new double[] { t.getMin().doubleValue(), t.getMax().doubleValue() });
				} else {
					m.getMap().setColorRange(null);
				}
			}

			if (plottedObjects.contains(m.getMap())) {
				plottedObjects.remove(m.getMap());
			} else {
				stale.add(m);
			}
		}
		
		layers.removeAll(stale);
		
		for (MapTrace t : stale) {
			if (t.getMap() instanceof LiveStreamMapObject) {
				try {
					((LiveStreamMapObject)t.getMap()).disconnect();
				} catch (Exception e) {
					logger.error("Could not disconnect stream!",e);
				}
			}
		}
		
		for (PlottableMapObject o : plottedObjects) {
			if (o instanceof LiveStreamMapObject){
				MapTrace t = new MapTrace(o, null, mapPlot);
				layers.addLast(t);
			} else if (o instanceof VectorMapData) {
				// Initialise a vector trace
				IVectorTrace vectorTrace = createVectorTrace((VectorMapData)o,mapPlot);
				layers.push(new MapTrace(o, vectorTrace, mapPlot));
			} else if (o instanceof AssociatedImage){
				MapTrace t = new MapTrace(o, null, mapPlot);
				layers.addLast(t);

			
			} else if (o instanceof ReMappedData && ((ReMappedData) o).getData().getSize() == 1 && !o.isLive()) {
				
				ILineTrace t = createPointTrace((ReMappedData)o, mapPlot);
				layers.push(new MapTrace(o, t, mapPlot));
				
			}else {

				IImageTrace t = createImageTrace(o,mapPlot);
				layers.push(new MapTrace(o, t, mapPlot));
			}
		}
		
		for (MapTrace t : layers) {
			if (t.getMap().isLive()) {
				t.getMap().update();
			}
			t.rebuildTrace();
		}
		
		plotLayers(layers, stale, mapPlot);
	}
	
	private void plotLayers(Deque<MapTrace> localLayers, List<MapTrace> stale, IPlottingSystem<?> mapPlot){
		
		if (Display.getCurrent() == null) {
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					plotLayers(localLayers, stale, mapPlot);
				}
			});
			return;
		}
		
		if (!mapPlot.is2D()) mapPlot.setPlotType(PlotType.IMAGE);
		
		mapPlot.clear();
		
		updatePlottedRange();
		
		try {

			Iterator<MapTrace> it = localLayers.descendingIterator();
			
			while (it.hasNext()) {
				MapTrace m = it.next();
				if (m.getTrace() != null) {
					 ITrace t = m.getTrace();
					 if (t instanceof IImageTrace) {
						 ((IImageTrace) t).setAlpha(m.getMap().getTransparency());
					 }
					 
					 if (t instanceof IPaletteTrace) {
						double[] colorRange = m.getMap().getColorRange();
						if (colorRange != null) {
							((IPaletteTrace) t).setMax(colorRange[1]);
							((IPaletteTrace) t).setMin(colorRange[0]);
							((IPaletteTrace) t).setRescaleHistogram(false);

						}
					 }
					 mapPlot.addTrace(m.getTrace());
				}
				if (m.getMap() instanceof LiveStreamMapObject) {
					if (m.getTrace() == null) {
						m.trace = createLiveStreamTrace((LiveStreamMapObject) m.getMap(), mapPlot);
						mapPlot.addTrace(m.getTrace());
					}
					LiveStreamMapObject l = (LiveStreamMapObject)m.getMap();
					l.addAxisListener(moveListener);
				}
			}
			
			//temporary fix forcing the origin to be top left
			
			IAxis xAxis = mapPlot.getSelectedXAxis();
			if (xAxis != null) xAxis.setInverted(false);
			
			IAxis yAxis = mapPlot.getSelectedYAxis();
			if (yAxis != null) yAxis.setInverted(true);
			
			it = localLayers.descendingIterator();
			while (it.hasNext()) {
				MapTrace m = it.next();
				if (m.getTrace() != null) {
					ITrace t = m.getTrace();
					if (t instanceof IImageTrace) {
						((IImageTrace) t).getImageServiceBean().setTransposed(false);
						((IImageTrace) t).getImageServiceBean().setOrigin(ImageOrigin.TOP_LEFT);

					}
				}
			}
			//end temporary fix
			
			mapPlot.repaint();
		} catch (Exception e) {
			logger.error("Error plotting mapped data", e);
		}
	}
	
	private IImageTrace createLiveStreamTrace(LiveStreamMapObject o, IPlottingSystem<?> mapPlot) {
		
		
		if (o.getDynamicDataset() == null) {
			connectStream(o);
		}
		
		IDynamicShape dataset = o.getDynamicDataset();
			
		if (dataset == null) return null;
		
		IImageTrace trace = mapPlot.createImageTrace(o.getPath());
		trace.setDynamicData(dataset);
		trace.setGlobalRange(o.getRange());
		trace.setAxes(o.getAxes(), false);
		return trace;
	
	}
	
	private void connectStream(LiveStreamMapObject o) {
		try {
			 o.connect();
		} catch (Exception e) {
			try {
				o.disconnect();
				o.connect();
			} catch (Exception e2) {
				logger.error("Could not connect to stream",e2);
			}
		}
	}

	public PlottableMapObject getTopMap(double x, double y){
		
		Iterator<MapTrace> iterator = layers.iterator();
		
		while (iterator.hasNext()) {
			MapObject l = iterator.next().getMap();
			double[] range = l.getRange();
			
			double x0 = range[0];
			double x1 = range[1];
			double y0 = range[2];
			double y1 = range[3];
			
			if (x0 == x1) {
				double scaling = getMapPlot().getSelectedXAxis().getScaling()*(POINTSIZE/2);
				x0 -= scaling;
				x1 += scaling;
			}
			
			if (y0 == y1) {
				double scaling = getMapPlot().getSelectedYAxis().getScaling()*(POINTSIZE/2);
				y1 -= scaling;
				y0 += scaling;
			}
			
			
			if (l instanceof PlottableMapObject && x >= x0 && x <= x1 && y >= y0 && y <= y1) return (PlottableMapObject)l;
			
		}
		
		return null;
	}
	
	public PlottableMapObject getTopMap(){
		
		Iterator<MapTrace> iterator = layers.iterator();
		
		while (iterator.hasNext()) {
			MapObject l = iterator.next().getMap();
			if (l instanceof PlottableMapObject) return (PlottableMapObject)l;
		}
		
		return null;
	}
	
	public void waitOnJob() {

		Future<?> future = atomicFuture.get();
		if (future != null) {
			try {
				future.get();
			} catch (Exception e) {
				logger.info("Error from future", e);
			} 
		}

	}
	
	private IImageTrace createImageTrace(MapObject ob, IPlottingSystem<?> mapPlot) {
		
		//this is not where live streams are created
		if (ob instanceof LiveStreamMapObject) {
			return null;
		}
		
		IDataset map = null;
		
		String longName = "";
		
		if (ob instanceof PlottableMapObject) {
			PlottableMapObject amd = (PlottableMapObject)ob;
			map = amd.getMap();
			longName = amd.getLongName();
		}
		
		
		if (map == null) return null;
		if (ob.getRange() == null) return null;
		IImageTrace t = null;
		try {
			t = MetadataPlotUtils.buildTrace(longName, map, mapPlot);
			t.setGlobalRange(sanizeRange(ob.getRange(), map.getShape()));
		} catch (Exception e) {
			logger.error("Error creating image trace", e);
		}
	
		return t;
	}
	
	private IVectorTrace createVectorTrace (VectorMapData ob, IPlottingSystem<?> mapPlot) {

		String longName = "";
		IDataset map = null;
		
		if (ob instanceof PlottableMapObject) {
			PlottableMapObject amd = (PlottableMapObject)ob;
			map = amd.getMap();
			longName = amd.getLongName();
		}
		
		if (map == null) return null;
		
		IVectorTrace vectorTrace = null;
		
		try {
			// Create the vector trace, the long way round
			vectorTrace = mapPlot.createVectorTrace(longName);
			vectorTrace.setVectorNormalization(VectorNormalization.LINEAR);
			vectorTrace.setArrowColor(new int[] {200, 0, 0});
			vectorTrace.setCircleColor(new int[] {0, 200, 0});
			vectorTrace.setArrowConfiguration(ArrowConfiguration.THROUGH_CENTER);
			vectorTrace.setRadians(false);
			vectorTrace.setPercentageThreshold(new double[] {5,95});

			// Set the vector data in the plot object
			vectorTrace.setData(ob.getMap(), ob.getAxes());
		}
		catch (Exception e) {
		logger.error("Error creating image trace", e);
		}
			
		// Return the trace
		return vectorTrace;
	}
	
	private ILineTrace createPointTrace(ReMappedData ob, IPlottingSystem<?> mapPlot) {
		
		IDataset m = ob.getMap();
		IDataset[] ax = MetadataPlotUtils.getAxesForDimension(m, 0);
		
		ILineTrace lt = mapPlot.createLineTrace(ob.getLongName());
		lt.setData(ax[1], ax[0]);
		lt.setPointStyle(PointStyle.FILLED_SQUARE);
		lt.setPointSize(POINTSIZE);
		
		return lt;
	}
	
	private void updatePlottedRange(){
		
		double[] range = null;
		
		Iterator<MapTrace> iterator = layers.iterator();
		
		while (iterator.hasNext()) {
			PlottableMapObject object = iterator.next().getMap();
			double[] r = object.getRange();
			if (r == null) continue;
			
			if (range == null) {
				range = r;
				continue;
			}
			
			range[0]  = r[0] < range[0] ? r[0] : range[0];
			range[1]  = r[1] > range[1] ? r[1] : range[1];
			range[2]  = r[2] < range[2] ? r[2] : range[2];
			range[3]  = r[3] > range[3] ? r[3] : range[3];
		}
		
		if (range == null) return;
		
		iterator = layers.iterator();
		
		while (iterator.hasNext()) {
			ITrace trace = iterator.next().getTrace();
			if (trace instanceof IImageTrace) {
				((IImageTrace)trace).setGlobalRange(range);
			}

		}
		
	}
	
	public void setTransparency(PlottableMapObject m) {
		if (!plotsReady()) return;
		
		Iterator<MapTrace> iterator = layers.iterator();

		while (iterator.hasNext()) {
			MapTrace l = iterator.next();
			if (l.getMap() == m && l.getTrace() instanceof IImageTrace) {
				((IImageTrace)l.getTrace()).setAlpha(m.getTransparency());
			}
		}
		
		map.repaint(false);
	}
	
	private Dataset getMergedDataset(IDataset input) {

		Dataset m = merge;

		if (m == null) {
			synchronized(this) {
				m = merge;
				if (m == null) {
					int[] newShape = ShapeUtils.squeezeShape(input.getShape(), false);
					for (int i = 0; i<newShape.length;i++) newShape[i]*=2;
					FloatDataset f = DatasetFactory.zeros(FloatDataset.class, newShape);
					Arrays.fill(f.getData(), Float.NaN);
					m = merge = f;

				}		
			}
		}
		return m;
	}
	
	private double[] sanizeRange(double[] range, int[] shape) {
		if(range[0] == range[1] && range[2] == range[3]) return range;
		
		double[] r = range.clone();
		
		if (range[0] == range[1]) {
			r[1] = range[0] + (range[3] - range[2])/shape[1];
			return r;
		}
		
		if (range[2] == range[3]) {
			r[3] = range[2] + (range[1] - range[0])/shape[0];
		}
		return r;
		
	}
	
	
	public static ILineTrace buildLineTraceFromImageSum(IDataset data, IPlottingSystem<?> system) {
		
		IDataset x = null;
		data = data.getSliceView().squeeze();
	
		IDataset[] axes = MetadataPlotUtils.getAxesFromMetadata(data);
		x = (axes != null && axes.length > 1 && axes[1] != null) ? axes[1].squeeze() : null;
		
		if (x != null && x.getRank() == 2) {
			x = x.getSlice(new Slice(0,1), null).squeeze();
		}
		
		ILineTrace it = system.createLineTrace(data.getName());
		Dataset sum = DatasetUtils.convertToDataset(data).sum(0, true);
		sum.setName(data.getName() + "_sum");
		it.setData(x, sum);

		return it;
	}
	
	public static ILineTrace[] buildLineTracesFromImage(IDataset data, IPlottingSystem<?> system) {
	
		IDataset x = null;
		data = data.getSliceView().squeeze();
		IDataset[] axes = MetadataPlotUtils.getAxesFromMetadata(data);
		x = (axes != null && axes.length > 1 && axes[1] != null) ? axes[1].squeeze() : null;
		
		if (x != null && x.getRank() == 2) {
			x = x.getSlice(new Slice(0,1), null).squeeze();
		}

		SliceViewIterator it = new SliceViewIterator(data, null, new int[] {1});
		int total = it.getTotal();
		ILineTrace[] traces = new ILineTrace[total];
		int count = 0;
		while (it.hasNext()) {
	
			ILazyDataset next = it.next();
			IDataset slice;
			try {
				slice = next.getSlice().squeeze();
			} catch (DatasetException e) {
				//shouldn't happen, not really a lazy dataset
				logger.error("Could not slice dataset", e);
				return null;
			}
	
			ILineTrace lt = system.createLineTrace(data.getName());
			slice.setName(data.getName() + "_" + it.getCurrent());
			lt.setData(x, slice);
			traces[count++] = lt;
		}
		return traces;
	
	}
	
	
	private class MapTrace {
		
		private PlottableMapObject map;
		private ITrace trace;
		private HistoInfo histoInfo = null;
		private IPlottingSystem<?> mapPlot;

		public MapTrace(PlottableMapObject map, ITrace trace, IPlottingSystem<?> mapPlot) {
			this.map = map;
			this.trace = trace;
			this.mapPlot = mapPlot;
		}

		public PlottableMapObject getMap() {
			return map;
		}

		public ITrace getTrace() {
			
			if (histoInfo != null) {
				IImageTrace t = (IImageTrace)trace;
				t.setRescaleHistogram(false);
				t.setMin(histoInfo.min);
				t.setMax(histoInfo.max);
				histoInfo = null;
			}
			
			return trace;
		}

		public void rebuildTrace(){
			
			if (trace instanceof IVectorTrace) {
				return;
			}
			
			if (trace == null && map instanceof ReMappedData && ((ReMappedData)map).getData().getSize() == 1) {
				trace = createPointTrace((ReMappedData)map, mapPlot);
				return;
			}
			
			if (trace instanceof ILineTrace && map instanceof ReMappedData) {
				trace = createPointTrace((ReMappedData)map, mapPlot);
				return;
			}
			
			Number min = null;
			Number max = null;
			boolean locked = false;
			
			if (trace != null && trace instanceof IImageTrace) {
				IImageTrace t = (IImageTrace)trace;
				locked = !t.isRescaleHistogram();
				if (locked) {
					min = t.getMin();
					max = t.getMax();
				}
			}
			
			trace = createImageTrace(map, mapPlot);
			
			if (locked && trace != null) {
				histoInfo = new HistoInfo(min, max);
			}
		}
		
		private class HistoInfo {
			
			public Number min;
			public Number max;

			public HistoInfo(Number min, Number max) {
				this.min = min;
				this.max = max;
			}
		}
	}
	
	private static class MapClickEvent implements IMapClickEvent {
		
		private final ClickEvent clickEvent;
		private final boolean isDoubleClick;
		private final String filePath;
		
		public MapClickEvent(ClickEvent clickEvent, boolean isDoubleClick,
				String filePath) {
			this.clickEvent = clickEvent;
			this.isDoubleClick = isDoubleClick;
			this.filePath = filePath;
		}

		@Override
		public ClickEvent getClickEvent() {
			return clickEvent;
		}

		@Override
		public boolean isDoubleClick() {
			return isDoubleClick;
		}

		@Override
		public String getFilePath() {
			return filePath;
		}
		
	}

	public static final String EVENT_TOPIC_MAPVIEW_CLICK = "org/dawnsci/mapping/ui/mapview/click";

	@Override
	public void bringToFront(PlottableMapObject map) {
		if (!map.isPlotted()) return;
		
		MapTrace mt = findRemoveMapTrace(map);
		
		if (mt == null) return;
		
		layers.push(mt);
		
		updatePlot();
		
	}

	@Override
	public void sendToBack(PlottableMapObject map) {
		if (!map.isPlotted()) return;
		
		MapTrace mt = findRemoveMapTrace(map);
		
		if (mt == null) return;
		
		layers.add(mt);
		
		updatePlot();
		
	}
	
	private MapTrace findRemoveMapTrace(PlottableMapObject map) {
		Iterator<MapTrace> iterator = layers.iterator();
		
		MapTrace mt = null;
		
		while (iterator.hasNext()) {
			MapTrace next = iterator.next();
			if (next.getMap() == map) {
				mt = next;
				iterator.remove();
				return mt;
			}
		}
		
		return null;
	}

	@Override
	public boolean initPlots() {
		return plotsReady();
	}
}
