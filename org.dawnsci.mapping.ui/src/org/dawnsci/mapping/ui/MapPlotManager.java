package org.dawnsci.mapping.ui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.MapObject;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ICompositeTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.swt.widgets.Display;

public class MapPlotManager {
	
	private IPlottingSystem map;
	private IPlottingSystem data;
	private MappedDataArea area;
	private List<MapObject> layers;
	private PlotJob job;
	private volatile Dataset merge;
	private AtomicInteger atomicPosition;
	
	public MapPlotManager(IPlottingSystem map, IPlottingSystem data, MappedDataArea area) {
		this.map = map;
		this.data = data;
		this.area = area;
		atomicPosition = new AtomicInteger(0);
		layers = new LinkedList<MapObject>();
		job = new PlotJob();
		job.setPriority(Job.INTERACTIVE);
		
	}
	
	public void plotData(final double x, final double y) {
		final MappedData topMap = getTopMap();
		if (topMap == null) return;
		merge = null;
		atomicPosition.set(0);
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				IDataset s = topMap.getSpectrum(x,y).getSlice();
				if (s != null) MappingUtils.plotDataWithMetadata(s, data, new int[]{0});
				
			}
		};
		
		job.setRunnable(r);
		
		job.schedule();
	}
	
	public void plotDataWithHold(final double x, final double y) {
		final MappedData topMap = getTopMap();
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
						System.err.println(pos);
						Dataset sub = null;
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
								
							}
						});
					}
					
				}
			};
		}
		
		job.setRunnable(r);
		
		job.schedule();
	}
	
	
	public void plotMap(MappedData map) {
		plotMapData(map);
	}
	
	public MappedData getTopMap(){
		
		for (int i = layers.size()-1; i >=0 ; i--) {
			MapObject l = layers.get(i);
			if (l instanceof MappedData) return (MappedData)l;
		}
		
		return null;
	}
	
	public void clearAll(){
		map.clear();
		data.clear();
	}
	
//	private int[] makeIndexArray() {
//		int[] out = new int[255];
//		
//		
//		
//		
//	}
	
	private void plotMapData(MappedData mapdata){
		map.clear();
		MappedDataFile dataFile = area.getDataFile(0);
		AssociatedImage image = dataFile.getAssociatedImage();
		if (mapdata == null) mapdata = dataFile.getMap();
		int count = 0;
		layers.clear();
		try {
			ICompositeTrace comp = this.map.createCompositeTrace("composite1");
			if (image != null) {
				layers.add(image);
				comp.add(MappingUtils.buildTrace(image.getImage(), this.map),count++);
			}

			layers.add(mapdata);
			comp.add(MappingUtils.buildTrace(mapdata.getMap(), this.map,mapdata.getTransparency()),count++);
			this.map.addTrace(comp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
}
