package org.dawnsci.mapping.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.ILiveData;
import org.dawnsci.mapping.ui.datamodel.MapObject;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
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
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapPlotManager {
	
	private IPlottingSystem<Composite> map;
	private IPlottingSystem<Composite> data;
	private MappedDataArea area;
	private LinkedList<MapTrace> layers;
	private PlotJob job;
	private RepeatingJob rJob;
	private volatile Dataset merge;
	private AtomicInteger atomicPosition;
	private int layerCounter = 0;
	
	private final static Logger logger = LoggerFactory.getLogger(MapPlotManager.class);
	
	public MapPlotManager(IPlottingSystem<Composite> map, IPlottingSystem<Composite> data, MappedDataArea area) {
		this.map = map;
		this.data = data;
		this.area = area;
		atomicPosition = new AtomicInteger(0);
		layers = new LinkedList<MapTrace>();
		job = new PlotJob();
		job.setPriority(Job.INTERACTIVE);
		rJob = new RepeatingJob(500, new Runnable() {
			
			@Override
			public void run() {
				for (MapTrace t : layers) {
					if (t.getMap() instanceof ILiveData) {
						t.switchMap(t.getMap());
					}
				}
				plotLayers();
			}
		});
		
		List<IAxis> axes = data.getAxes();
		for (IAxis axis : axes) axis.setAxisAutoscaleTight(true);
		
	}
	
	public void plotData(final double x, final double y) {
		final AbstractMapData topMap = getTopMap(x,y);
		if (topMap == null)  {
			data.clear();
			return;
		}
		merge = null;
		atomicPosition.set(0);
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				IDataset s = topMap.getSpectrum(x,y);
				if (s == null) {
					data.clear();
					return;
				}
				
//				IDataset s = l.getSlice();
				
				if (s != null) MetadataPlotUtils.plotDataWithMetadata(s, data);
				
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
				
			}
		};
		
		job.setRunnable(r);
		
		job.schedule();
	}
	
	public void plotDataWithHold(final double x, final double y) {
		final AbstractMapData topMap = getTopMap(x,y);
		if (topMap == null) return;
		final ILazyDataset lz = topMap.getSpectrum(x,y);
		
		Runnable r = null;
		
		if (AbstractDataset.squeezeShape(lz.getShape(),false).length > 1) {
			job.cancel();
			
			r = new Runnable() {
				
				@Override
				public void run() {
					IDataset s = lz.getSlice().squeeze();
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
					IDataset s = lz.getSlice();
					if (s != null) {
						final ILineTrace l = MetadataPlotUtils.buildLineTrace(s, data);
						
						Display.getDefault().syncExec(new Runnable() {
							
							@Override
							public void run() {
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
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								
							}
						});
					}
					
				}
			};
		}
		
		job.setRunnable(r);
		
		job.schedule();
	}
	
	
	public void updateLayers(AbstractMapData map) {
		
		Iterator<MapTrace> it = layers.iterator();
		
		while (it.hasNext()) {
			MapTrace m = it.next();
			if (m.getMap() == map) {
				it.remove();
				this.map.removeTrace(m.getTrace());
				triggerForLive();
				plotLayers();
				return;
			}
		}
		
		if (map == null) {
			for (int i = 0; i < area.count();i++) {
				
				AssociatedImage associatedImage = area.getDataFile(i).getAssociatedImage();
				if (associatedImage != null) {
					addImage(associatedImage);
				}
				
				map = area.getDataFile(i).getMap();
				if (map == null) continue;
				addMap(map);
			}
 			
		} else {
			addMap(map);
		}
		
		plotLayers();
	}
	
	public void plotMap(AbstractMapData map) {
		addMap(map);
		plotLayers();
	}
	
	public void addImage(AssociatedImage image) {
		Iterator<MapTrace> it = layers.iterator();
		
		while (it.hasNext()) {
			MapTrace m = it.next();
			if (m.getMap() == image) {
				it.remove();
				this.map.removeTrace(m.getTrace());
				plotLayers();
				return;
			}
		}	

		layers.addLast(new MapTrace(image, null));
		map.clear();
		for (MapTrace l : layers) l.rebuildTrace();
		
		plotLayers();
	}
	
	public AbstractMapData getTopMap(double x, double y){
		
		for (int i = 0; i < layers.size() ; i++) {
			MapObject l = layers.get(i).getMap();
			if (l instanceof AbstractMapData && ((AbstractMapData)l).getSpectrum(x, y) != null) return (AbstractMapData)l;
		}
		
		return null;
	}
	
	public MappedData getTopMap(){
		
		for (int i = 0; i < layers.size() ; i++) {
			MapObject l = layers.get(i).getMap();
			if (l instanceof AbstractMapData) return (MappedData)l;
		}
		
		return null;
	}
	
	public void clearAll(){
		map.clear();
		data.clear();
		layers.clear();
	}
	
	private void addMap(AbstractMapData map) {

		int position = -1;
		
		for (int i = 0; i < layers.size() ;i++) {
			MapObject layer = layers.get(i).getMap();
			if (layer instanceof AbstractMapData && isTheSameMap((AbstractMapData)layer, map)) {
				position = i;
				break;
			}
		}
		
		if (position >= 0) {
			MapTrace mapTrace = layers.get(position);
			mapTrace.switchMap(map);
			
//			layers.set(position, map);
		} else {
			IImageTrace t = createImageTrace(map);
			if (t == null) return;
			layers.push(new MapTrace(map, t));
		}
	
		triggerForLive();
	}
	
	private void triggerForLive(){
		boolean found = false;
		
		for (int i = 0; i < layers.size() ;i++) {
			if (layers.get(i).getMap() instanceof ILiveData) {
				found = true;
				break;
			}
		}
		
		if (found){
			rJob.start();
			rJob.schedule();
		}
		else rJob.stop();
	}
	
	private IImageTrace createImageTrace(MapObject ob) {
		
		String longName = "Layer " + layerCounter++;
		IDataset map = null;
		
		if (ob instanceof PlottableMapObject) {
			PlottableMapObject amd = (PlottableMapObject)ob;
//			longName = amd.getLongName();
			map = amd.getData();
		}
		
		if (map == null) return null;
		IImageTrace t = null;
		try {
			t = MetadataPlotUtils.buildTrace(longName, map, this.map);
			t.setGlobalRange(sanizeRange(area.getRange(), map.getShape()));
			if (ob instanceof AbstractMapData)  t.setAlpha(((AbstractMapData)ob).getTransparency());
		} catch (Exception e) {
			//TODO
		}
		
		
		return t;
	}
	

	
	private void plotLayers(){
		if (!map.is2D()) map.setPlotType(PlotType.IMAGE);
//		map.clear();
		try {
			
			if (layers.isEmpty()) {
				map.clear();
				layerCounter = 0;
				return;
			} else {
				
			}
			
			Iterator<MapTrace> it = layers.descendingIterator();
			
			Collection<ITrace> traces = map.getTraces(IImageTrace.class);
			
			while (it.hasNext()) {
				MapTrace m = it.next();
				if (!traces.contains(m.getTrace())) {
					map.addTrace(m.getTrace());
				}
//				MapObject o = it.next();
//				IImageTrace trace = createImageTrace(o);
//				if (trace != null)map.addTrace(trace);
//				if (o instanceof AbstractMapData) {
//					IImageTrace trace = createImageTrace((AbstractMapData)o);
//					map.addTrace(trace);
//					
////					IDataset[] ax = MappingUtils.getAxesFromMetadata(m.getMap());
////					ILineTrace lt = map.createLineTrace(m.getLongName()+ "line");
////					lt.setData(ax[1], ax[0]);
////					map.addTrace(lt);
//				}
//				
//				if (o instanceof AssociatedImage) {
////					comp.add(MappingUtils.buildTrace(((AssociatedImage)o).getImage(), this.map),count++);
//					AssociatedImage im = (AssociatedImage)o;
//					IImageTrace t = MappingUtils.buildTrace(im.getLongName(),im.getImage(), this.map);
//					t.setGlobalRange(area.getRange());
//					this.map.addTrace(t);
//				}
				
			}
			this.map.repaint();
		} catch (Exception e) {
			logger.error("Error plotting mapped data", e);
		}
	}
	
	private boolean isTheSameMap(AbstractMapData omap, AbstractMapData map) {
		
		if (omap.getLongName().equals(map.getLongName())) return true;
		
		if (omap.getData() == null ||  map.getData() == null) return false;
		
		if (!Arrays.equals(omap.getData().getShape(), map.getData().getShape())) return false;
		
		AxesMetadata oax = omap.getData().getFirstMetadata(AxesMetadata.class);
		AxesMetadata ax = map.getData().getFirstMetadata(AxesMetadata.class);
		
		if (oax == null || ax == null) return false; // should never be the case
		
		ILazyDataset[] oaxes = oax.getAxes();
		ILazyDataset[] axes = ax.getAxes();
		
		if (oaxes.length != axes.length) return false;
		
		for (int i = 0 ; i < oaxes.length; i++) {
			if (oaxes[i] == null) return false;
			if (axes[i] == null) return false;
		
			IDataset oa = oaxes[i].getSlice();
			IDataset a = axes[i].getSlice();
			if (!oa.equals(a)) return false;
		}
		
		return true;
		
	}
	
	public void setTransparency(AbstractMapData m) {
		
		for (int i = 0; i < layers.size() ;i++) {
			MapTrace layer = layers.get(i);
			if (layer.getMap() == m) layer.getTrace().setAlpha(m.getTransparency());
		}
		
		map.repaint(false);
	}
	
//	private void plotMapData(){
//		map.clear();
//		MappedDataFile dataFile = area.getDataFile(0);
//		AssociatedImage image = dataFile.getAssociatedImage();
//		if (mapdata == null) mapdata = dataFile.getMap();
//		int count = 0;
//		try {
//			ICompositeTrace comp = this.map.createCompositeTrace("composite1");
//			if (image != null) {
//				layers.add(image);
//				comp.add(MappingUtils.buildTrace(image.getImage(), this.map),count++);
//			}
//
//			layers.add(mapdata);
//			comp.add(MappingUtils.buildTrace(mapdata.getMap(), this.map,mapdata.getTransparency()),count++);
//			this.map.addTrace(comp);
//		} catch (Exception e) {
//			logger.error("Error plotting mapped data", e);
//		}
//	}
	
	public boolean isPlotted(MapObject object) {
		
		for (MapTrace t : layers) {
			if (t.getMap().equals(object)) return true;
		}
		
		return false;
	}
	
	private Dataset getMergedDataset(IDataset input) {

		Dataset m = merge;

		if (m == null) {
			synchronized(this) {
				m = merge;
				if (m == null) {
					int[] newShape = AbstractDataset.squeezeShape(input.getShape(), false);
					for (int i = 0; i<newShape.length;i++) newShape[i]*=2;
					FloatDataset f = new FloatDataset(newShape);
					Arrays.fill(f.getData(), Float.NaN);
					m = merge = f;

				}		
			}
		}
		return m;
	}
	
//	private class SingleTaskExecutor implements Executor {
//	    private final AtomicReference<Runnable> lastTask =new AtomicReference<>();
//	    private final Executor executor;
//
//	    public SingleTaskExecutor(Executor executor) {
//	        super();
//	        this.executor = executor;
//	    }
//
//	    @Override
//	    public void execute(Runnable command) {
//	        lastTask.set(command);
//	        executor.execute(new Runnable() {
//	            @Override
//	            public void run() {
//	                Runnable task=lastTask.getAndSet(null);
//	                if(task!=null){
//	                    task.run();
//	                }
//	            }
//	        });
//
//	    }
//	}
	
	private class PlotJob extends Job {

		private final AtomicReference<Runnable> task =new AtomicReference<Runnable>();
		
		public PlotJob() {
			super("Plot point...");
		}
		
		public void setRunnable(Runnable runnable) {
			this.task.set(runnable);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Runnable local = task.get();
			local.run();
			return Status.OK_STATUS;
		}
		
	}
	
	public void test_method_remove(IDataset m) {
		
		map.updatePlot2D(m, null, null);
		
		
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
	
	private class RepeatingJob extends UIJob{
		private boolean running = true;
		private long repeatDelay = 0;
		private Runnable runnable;
		public RepeatingJob(long repeatPeriod, Runnable runnable){ 
			super("Repeat plot update");
			repeatDelay = repeatPeriod;
			this.runnable = runnable;
		}
		public IStatus runInUIThread(IProgressMonitor monitor) {
			runnable.run();
			schedule(repeatDelay);
			return Status.OK_STATUS;
		}

		public boolean shouldSchedule() {
			return running;
		}
		public void stop() {
			running = false;
		}
		public void start() {
			running = true;
		}

	}
	
	private class MapTrace {
		
		private PlottableMapObject map;
		private IImageTrace trace;

		public MapTrace(PlottableMapObject map, IImageTrace trace) {
			this.map = map;
			this.trace = trace;
		}

		public PlottableMapObject getMap() {
			return map;
		}

		public IImageTrace getTrace() {
			return trace;
		}
		
		public void switchMap(PlottableMapObject ob) {
			try {
				IDataset d = ob.getData();
				MetadataPlotUtils.switchData(ob.getLongName(),d, trace);
				trace.setGlobalRange(sanizeRange(area.getRange(),d.getShape()));
				map = ob;
			} catch (Exception e) {
				logger.error("Error updating live!",e);
			}
			
		}
		
		public void rebuildTrace(){
			trace = createImageTrace(map);
		}
		
	}
}
