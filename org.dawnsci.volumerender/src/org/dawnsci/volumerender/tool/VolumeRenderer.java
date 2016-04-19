package org.dawnsci.volumerender.tool;

import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeRenderTrace;
import org.eclipse.swt.widgets.Display;

public class VolumeRenderer {

	private final IPlottingSystem<?> plottingSystem;
	private final String traceID;
	private final double resolution;
	private final double intensity;
	private final double opacity;
	private final ILazyDataset dataset;
	private final double[] minMaxValue;
	private final double[] minMaxCull;

	public VolumeRenderer(IPlottingSystem<?> plottingSystem, String traceID, double resolution, double intensity,
			double opacity, ILazyDataset dataset, double[] minMaxValue, double[] minMaxCull) {
				this.plottingSystem = plottingSystem;
				this.traceID = traceID;
				this.resolution = resolution;
				this.intensity = intensity;
				this.opacity = opacity;
				this.dataset = dataset;
				this.minMaxValue = minMaxValue;
				this.minMaxCull = minMaxCull;
	}

	public void run() {
		final IVolumeRenderTrace trace = createOrLookupTrace();
		
		int[] step = IntStream.rangeClosed(0, 2).map(i -> calculateStepSize(dataset,i)).toArray();
					
		trace.setData(
				dataset.getShape(), 
				dataset.getSlice(new int[]{0,0,0}, dataset.getShape(), step),
				intensity,
				opacity,
				minMaxValue,
				minMaxCull);
				
		if ((IVolumeRenderTrace) plottingSystem.getTrace(traceID) == null)
		{
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					plottingSystem.addTrace(trace);
		    	}
		    });
		}
	}

	private IVolumeRenderTrace createOrLookupTrace() {
		if ((IVolumeRenderTrace) plottingSystem.getTrace(traceID) == null){
			IVolumeRenderTrace trace = plottingSystem.createVolumeRenderTrace(traceID);
			trace.setName(traceID);
			return trace;
		} else{
			return (IVolumeRenderTrace) plottingSystem.getTrace(traceID);
		}
	}
	
	private int calculateStepSize(ILazyDataset dataset, int dimention){
		return (int)((dataset.getShape()[dimention] / (dataset.getShape()[dimention] * resolution) + 0.5f));
	}
}
