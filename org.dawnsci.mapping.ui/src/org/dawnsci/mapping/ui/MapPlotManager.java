package org.dawnsci.mapping.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.ILiveData;
import org.dawnsci.mapping.ui.datamodel.MapObject;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
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
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapPlotManager {
	
	private IPlottingSystem<Composite> map;
	private IPlottingSystem<Composite> data;
	private MappedDataArea area;
	private LinkedList<MapObject> layers;
	private PlotJob job;
	private RepeatingJob rJob;
	private volatile Dataset merge;
	private AtomicInteger atomicPosition;
	
	private final static Logger logger = LoggerFactory.getLogger(MapPlotManager.class);
	
	public MapPlotManager(IPlottingSystem<Composite> map, IPlottingSystem<Composite> data, MappedDataArea area) {
		this.map = map;
		this.data = data;
		this.area = area;
		atomicPosition = new AtomicInteger(0);
		layers = new LinkedList<MapObject>();
		job = new PlotJob();
		job.setPriority(Job.INTERACTIVE);
		rJob = new RepeatingJob(500, new Runnable() {
			
			@Override
			public void run() {
				plotLayers();
			}
		});
		
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
				ILazyDataset l = topMap.getSpectrum(x,y);
				if (l == null) {
					data.clear();
					return;
				}
				
				IDataset s = l.getSlice();
				
				if (s != null) MappingUtils.plotDataWithMetadata(s, data, new int[]{0});
				
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
						MappingUtils.plotDataWithMetadata(mergedDataset, data, null);
								
						
					}
				}
			};
			
			
			
		} else {
			r = new Runnable() {
				
				@Override
				public void run() {
					IDataset s = lz.getSlice();
					if (s != null) {
						final ILineTrace l = MappingUtils.buildLineTrace(s, data);
						
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
		if (layers.contains(map)){
			layers.remove(map);
			triggerForLive();
			plotLayers();
			return;
		}
		
		if (map == null) {
			for (int i = 0; i < area.count();i++) {
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
		if (layers.contains(image)){
			layers.remove(image);
			plotLayers();
			return;
		}
		layers.addLast(image);
		plotLayers();
	}
	
	public AbstractMapData getTopMap(double x, double y){
		
		for (int i = 0; i < layers.size() ; i++) {
			MapObject l = layers.get(i);
			if (l instanceof AbstractMapData && ((AbstractMapData)l).getSpectrum(x, y) != null) return (AbstractMapData)l;
		}
		
		return null;
	}
	
	public MappedData getTopMap(){
		
		for (int i = 0; i < layers.size() ; i++) {
			MapObject l = layers.get(i);
			if (l instanceof MappedData) return (MappedData)l;
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
			MapObject layer = layers.get(i);
			if (layer instanceof MappedData && isTheSameMap((AbstractMapData)layer, map)) {
				position = i;
				break;
			}
		}
		
		if (position >= 0) {
			layers.set(position, map);
		} else {
			layers.push(map);
		}
	
		triggerForLive();
	}
	
	private void triggerForLive(){
		boolean found = false;
		
		for (int i = 0; i < layers.size() ;i++) {
			if (layers.get(i) instanceof ILiveData) {
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
	
	private void plotLayers(){

		map.clear();
		
		try {
			
			Iterator<MapObject> it = layers.descendingIterator();
			
			while (it.hasNext()) {
				MapObject o = it.next();
				if (o instanceof MappedData) {
					MappedData m = (MappedData)o;
					IImageTrace t = MappingUtils.buildTrace(m.getLongName(),m.getMap(), this.map);
					t.setGlobalRange(area.getRange());
					t.setAlpha(((MappedData)o).getTransparency());
					this.map.addTrace(t);
					
//					IDataset[] ax = MappingUtils.getAxesFromMetadata(m.getMap());
//					ILineTrace lt = map.createLineTrace(m.getLongName()+ "line");
//					lt.setData(ax[1], ax[0]);
//					map.addTrace(lt);
				}
				
				if (o instanceof AssociatedImage) {
//					comp.add(MappingUtils.buildTrace(((AssociatedImage)o).getImage(), this.map),count++);
					AssociatedImage im = (AssociatedImage)o;
					IImageTrace t = MappingUtils.buildTrace(im.getLongName(),im.getImage(), this.map);
					t.setGlobalRange(area.getRange());
					this.map.addTrace(t);
				}
				
			}
			this.map.repaint();
		} catch (Exception e) {
			logger.error("Error plotting mapped data", e);
		}
	}
	
	private boolean isTheSameMap(AbstractMapData omap, AbstractMapData map) {
		
		if (omap.getLongName().equals(map.getLongName())) return true;
		
		if (!Arrays.equals(omap.getMap().getShape(), map.getMap().getShape())) return false;
		
		AxesMetadata oax = omap.getMap().getFirstMetadata(AxesMetadata.class);
		AxesMetadata ax = map.getMap().getFirstMetadata(AxesMetadata.class);
		
		if (oax == null || ax == null) return false; // should never be the case
		
		ILazyDataset[] oaxes = oax.getAxes();
		ILazyDataset[] axes = ax.getAxes();
		
		if (oaxes.length != axes.length) return false;
		
		for (int i = 0 ; i < oaxes.length; i++) {
			if (oaxes[i] == null) return false;
			if (axes[i] == null) return false;
			
			IDataset oa = DatasetUtils.convertToDataset(oaxes[i]);
			IDataset a = DatasetUtils.convertToDataset(axes[i]);
			if (!oa.equals(a)) return false;
		}
		
		return true;
		
	}
	
	public void setTransparency(AbstractMapData m) {
		
		ITrace trace = map.getTrace(m.getLongName());
		if (trace instanceof IImageTrace) ((IImageTrace)trace).setAlpha(m.getTransparency());
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
		return layers.contains(object);
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
}
