package org.dawnsci.mapping.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.IMapFileEventListener;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject.IAxisMoveListener;
import org.dawnsci.mapping.ui.datamodel.MapObject;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.dawnsci.mapping.ui.datamodel.VectorMapData;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IVectorTrace;
import org.eclipse.dawnsci.plotting.api.trace.IVectorTrace.ArrowConfiguration;
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
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapPlotManager {
	
	private IPlottingSystem<Composite> map;
	private IPlottingSystem<Composite> data;
	private ConcurrentLinkedDeque<MapTrace> layers;
	private ExecutorService executor;
	private ExecutorService detectorExecutor;
	private AtomicReference<Runnable> atomicRunnable = new AtomicReference<>();
	private volatile Dataset merge;
	private AtomicInteger atomicPosition;
	private boolean firstHold = true;
	private IAxisMoveListener moveListener;
	
	private IMapFileController fileManager;
	
	private static final Logger logger = LoggerFactory.getLogger(MapPlotManager.class);
	
	public MapPlotManager(IPlottingSystem<Composite> map, IPlottingSystem<Composite> data) {
		this.map = map;
		this.data = data;
		
		executor = Executors.newSingleThreadExecutor();
		detectorExecutor = Executors.newSingleThreadExecutor();
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		this.fileManager = bundleContext.getService(bundleContext.getServiceReference(IMapFileController.class));
		
		atomicPosition = new AtomicInteger(0);
		layers = new ConcurrentLinkedDeque<MapTrace>();
		
		if (data.getAxes() != null) {
			List<IAxis> axes = data.getAxes();
			for (IAxis axis : axes) axis.setAxisAutoscaleTight(true);
		}
		
		fileManager.addListener(new IMapFileEventListener() {
			
			@Override
			public void mapFileStateChanged(MappedDataFile file) {
				updatePlot();
			}
		});
		
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
	
	public void updatePlot(){	
		Runnable r = atomicRunnable.getAndSet(new Runnable() {
			
			@Override
			public void run() {
				updateState();
				
			}
		});
		
		if (r == null) {
			executor.submit(()->{
				Runnable run = atomicRunnable.getAndSet(null);
				if (run == null) return;
				run.run();
			});
		}
		
	}
	
	public void plotData(final double x, final double y) {
		final PlottableMapObject topMap = getTopMap(x,y);
		if (topMap == null)  {
			data.clear();
			return;
		}
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
						data.clear();
						return;
					}
					logger.info("Slice time {} ms for shape {} of {}", (System.currentTimeMillis()-t), Arrays.toString(s.getShape()), topMap.toString());
					if (s.getSize() == 1) return;

					MetadataPlotUtils.plotDataWithMetadata(s, data);

					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							Collection<IRegion> regions = map.getRegions();
							Iterator<IRegion> it = regions.iterator();
							while (it.hasNext()) {
								IRegion r = it.next();
								if (r.getUserObject() ==  MapPlotManager.this){
									map.removeRegion(r);
								}
							}
							map.repaint(false);
						}

					});
				} catch (Exception e) {
					logger.error("Error plotting spectrum");
				}

			}
		};
		
		detectorExecutor.submit(r);
	}
	
	public void plotDataWithHold(final double x, final double y) {
		final PlottableMapObject topMap = getTopMap(x,y);
		if (topMap == null) return;
		final ILazyDataset lz = topMap.getSpectrum(x,y);
		
		Runnable r = null;
		
		if (ShapeUtils.squeezeShape(lz.getShape(),false).length > 1) {
			
			r = new Runnable() {
				
				@Override
				public void run() {
					if (firstHold) data.clear();
					firstHold = false;
					IDataset s = null;
					try {
						s = lz.getSlice().squeeze();
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
						MetadataPlotUtils.plotDataWithMetadata(mergedDataset, data);
								
						
					}
				}
			};
			
			
			
		} else {
			r = new Runnable() {
				
				@Override
				public void run() {
					IDataset s = null;
					try {
						s = lz.getSlice();
					} catch (DatasetException e) {
						logger.error("Could not get data from lazy dataset", e);
						return;
					}
					if (s != null) {
						final ILineTrace l = MetadataPlotUtils.buildLineTrace(s, data);
						
						Display.getDefault().syncExec(new Runnable() {
							
							@Override
							public void run() {
								if (firstHold) data.clear();
								firstHold = false;
								data.addTrace(l);
								try {
									String uniqueName = RegionUtils.getUniqueName("Click", map);
									final IRegion re = map.createRegion(uniqueName, RegionType.POINT);
									re.setROI(new PointROI(x, y));
									re.setAlpha(255);
									re.setRegionColor(l.getTraceColor());
									re.setUserObject(MapPlotManager.this);
									re.addROIListener(new IROIListener.Stub() {
										@Override
										public void roiSelected(ROIEvent evt) {
											Collection<ITrace> ts = data.getTraces();
											for (ITrace t : ts) if (t instanceof ILineTrace) ((ILineTrace)t).setLineWidth(1);
											l.setLineWidth(2);

										}
									});
//									((AbstractSelectionRegion)re).setHighlighted(true);
									map.addRegion(re);
									re.setMobile(false);
									re.setUserRegion(false);
									re.setUserObject(MapPlotManager.this);
									map.clearRegionTool();
									data.repaint();
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
	

	private void updateState() {
		List<PlottableMapObject> plottedObjects = fileManager.getPlottedObjects();
		
		if (plottedObjects.isEmpty()) {
			layers.clear();
			map.clear();
			data.clear();
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
				MapTrace t = new MapTrace(o, null);
				layers.addLast(t);
			} else if (o instanceof VectorMapData) {
				// Initialise a vector trace
				IVectorTrace vectorTrace = createVectorTrace(o);
				layers.push(new MapTrace(o, vectorTrace));
			} else if (o instanceof AssociatedImage){
				MapTrace t = new MapTrace(o, null);
				layers.addLast(t);

			}else {

				IImageTrace t = createImageTrace(o);
				layers.push(new MapTrace(o, t));
			}
		}
		
		for (MapTrace t : layers) {
			if (t.getMap().isLive()) {
				t.getMap().update();
			}
			t.rebuildTrace();
		}
		
		plotLayers(layers, stale);
	}
	
	private void plotLayers(Deque<MapTrace> localLayers, List<MapTrace> stale){
		
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				
				@Override
				public void run() {
					plotLayers(localLayers, stale);
				}
			});
			return;
		}
		
		if (!map.is2D()) map.setPlotType(PlotType.IMAGE);
		
		map.clearTraces();
		
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
					map.addTrace(m.getTrace());
				}
				if (m.getMap() instanceof LiveStreamMapObject) {
					if (m.getTrace() == null) {
						m.trace = createLiveStreamTrace((LiveStreamMapObject) m.getMap());
						map.addTrace(m.getTrace());
					}
					LiveStreamMapObject l = (LiveStreamMapObject)m.getMap();
					l.addAxisListener(moveListener);
				}
			}
			this.map.repaint();
		} catch (Exception e) {
			logger.error("Error plotting mapped data", e);
		}
	}
	
	private IImageTrace createLiveStreamTrace(LiveStreamMapObject o) {
		
		IDynamicShape dataset = null;
		
		try {
			 dataset = o.connect();
		} catch (Exception e) {
			try {
				 
			o.disconnect();
			dataset = o.connect();
			} catch (Exception e2) {
				logger.error("Could not connect to stream",e2);
			}
		}
			
		if (dataset == null) return null;
		
		IImageTrace trace = map.createImageTrace(o.getPath());
		trace.setDynamicData(dataset);
		trace.setGlobalRange(o.getRange());
		trace.setAxes(o.getAxes(), false);
		return trace;
	
	}

	public PlottableMapObject getTopMap(double x, double y){
		
		Iterator<MapTrace> iterator = layers.iterator();
		
		while (iterator.hasNext()) {
			MapObject l = iterator.next().getMap();
			double[] range = l.getRange();
			
			if (l instanceof PlottableMapObject && x >= range[0] && x <= range[1] && y >= range[2] && y <= range[3]) return (PlottableMapObject)l;
			
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
	
	private IImageTrace createImageTrace(MapObject ob) {
		
		
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
			t = MetadataPlotUtils.buildTrace(longName, map, this.map);
			t.setGlobalRange(sanizeRange(ob.getRange(), map.getShape()));
		} catch (Exception e) {
			logger.error("Error creating image trace", e);
		}
	
		return t;
	}
	
	private IVectorTrace createVectorTrace (MapObject ob) {

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
			vectorTrace = this.map.createVectorTrace(longName);
			// Get the axes
			AxesMetadata axesMetadata = map.getFirstMetadata(AxesMetadata.class);
			IDataset yAxis = DatasetUtils.sliceAndConvertLazyDataset(axesMetadata.getAxis(0)[0]).squeeze();
			IDataset xAxis = DatasetUtils.sliceAndConvertLazyDataset(axesMetadata.getAxis(1)[0]).squeeze();
			// Set the map datapoints and axes
			vectorTrace.setData(map, Arrays.asList(xAxis, yAxis));
			// And whilst setting up the plot, also define some plot specific options
			vectorTrace.setArrowColor(new int[] {200, 0, 0});
			vectorTrace.setCircleColor(new int[] {0, 200, 0});
			vectorTrace.setArrowConfiguration(ArrowConfiguration.THROUGH_CENTER);
		}
		catch (Exception e) {
		logger.error("Error creating image trace", e);
		}
			
		// Return the trace
		return vectorTrace;
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
	
	private class MapTrace {
		
		private PlottableMapObject map;
		private ITrace trace;
		private HistoInfo histoInfo = null;

		public MapTrace(PlottableMapObject map, ITrace trace) {
			this.map = map;
			this.trace = trace;
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
			
			trace = createImageTrace(map);
			
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
}
