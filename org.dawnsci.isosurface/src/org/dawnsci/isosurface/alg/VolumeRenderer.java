package org.dawnsci.isosurface.alg;

import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeRenderTrace;
import org.eclipse.swt.widgets.Display;

public class VolumeRenderer {

	private final IPlottingSystem<?> plottingSystem;
	private final String traceID;
	private final double resolution;
	private final double intensity;
	private final double opacity;
	private final double minValue;
	private final double maxValue;
	private final double minCull;
	private final double maxCull;
	private final int[] rgb;
	
	private final ILazyDataset dataset;

	public VolumeRenderer(IPlottingSystem<?> plottingSystem, String traceID, double resolution, double intensity,
			double opacity, double minValue, double maxValue, double minCull, double maxCull, int[] rgb, ILazyDataset dataset) {
				this.plottingSystem = plottingSystem;
				this.traceID = traceID;
				this.resolution = resolution;
				this.intensity = intensity;
				this.opacity = opacity;
				this.minValue = minValue;
				this.maxValue = maxValue;
				this.minCull = minCull;
				this.maxCull = maxCull;
				this.rgb = rgb;
				this.dataset = dataset;
	}

	public void run(IMonitor monitor) throws Exception{
		
		int[] step = IntStream.rangeClosed(0, 2).map(i -> calculateStepSize(dataset,i)).toArray();
			
		IDataset slice = dataset.getSlice(monitor, new int[]{0,0,0}, dataset.getShape(), step);
		
		double sliceMin = slice.min().doubleValue();
		double sliceMax = slice.max().doubleValue();
		
		double[] scaledMinMaxValue = scaledMinAndMax(minValue, maxValue, sliceMin, sliceMax);
		double[] scaledMinMaxCull = scaledMinAndMax(minCull, maxCull, sliceMin, sliceMax);
		
		final IVolumeRenderTrace trace = createOrLookupTrace();
		//TODO fix last argument
		trace.setData(dataset.getShape(), slice, intensity, opacity, scaledMinMaxValue, scaledMinMaxCull, null);
		trace.setColour(rgb[0], rgb[1], rgb[2]);
		if ((IVolumeRenderTrace) plottingSystem.getTrace(traceID) == null)
		{
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					plottingSystem.addTrace(trace);
		    	}
		    });
		}
	}

	private double[] scaledMinAndMax(double proportionMin, double proportionMax, double sliceMin, double sliceMax) {
		double difference = sliceMax - sliceMin;
		return new double[]{sliceMin + (difference * proportionMin), sliceMin + (difference * proportionMax)};
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
