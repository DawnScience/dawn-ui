package org.dawnsci.volumerender.tool;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeRenderTrace;
import org.eclipse.swt.widgets.Display;

public class VolumeRenderJob extends Job
{
 	final private IPlottingSystem<?> system;
 	private String traceID;
 	private ILazyDataset dataset;

 	private double resolution;
 	private double intensityValue;
 	private double opacityValue;
 	private double[] minMaxValue;
 	private double[] minMaxCulling;
 	
 	@SuppressWarnings("unused")
	private double opacity;
 	
	public VolumeRenderJob(String name, IPlottingSystem<?> system) 
	{
		super(name);
		
		this.system = system;
	}
	
	public void compute(
			final String traceID, 
			final double resolution, 
			final double intensity, 
			final double opacity, 
			final ILazyDataset dataset,
			final double[] minMaxValue,
			final double[] minMaxCulling)
	{	
		this.traceID = traceID;
		this.resolution = resolution / 100;
		this.intensityValue = intensity / 100;
		this.opacityValue = opacity / 100;
		this.dataset = dataset;
		this.minMaxValue = minMaxValue;
		this.minMaxCulling = minMaxCulling;
				
		cancel();
		schedule();
	}

	public void destroy(String traceID)
	{
		if (system.getTrace(traceID) != null)
		{ 
			system.getTrace(traceID).dispose();
			system.removeTrace(system.getTrace(traceID));
		}
	}
	
	public void setColour(int red, int green, int blue)
	{
		if (system.getTrace(traceID) != null)
		{ 
			((IVolumeRenderTrace)system.getTrace(traceID)).setColour(red, green, blue);
		}
	}
	
	public void setOpacity(double opacity)
	{
		this.opacity = opacity;
		
		if (system.getTrace(traceID) != null)
		{ 
			((IVolumeRenderTrace)system.getTrace(traceID)).setOpacity(opacity);
		}
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) 
	{
		Thread.currentThread().setName("generating volume renderer");
		
		final IVolumeRenderTrace trace;
		
		if ((IVolumeRenderTrace) system.getTrace(traceID) == null)
		{
			trace = system.createVolumeRenderTrace(traceID);
			trace.setName(traceID);
		}
		else
		{
			trace = (IVolumeRenderTrace) system.getTrace(traceID);
		}
		
		int[] step = {
				(int)((dataset.getShape()[0] / (dataset.getShape()[0] * resolution) + 0.5f)),
				(int)((dataset.getShape()[1] / (dataset.getShape()[1] * resolution) + 0.5f)),
				(int)((dataset.getShape()[2] / (dataset.getShape()[2] * resolution) + 0.5f))};
					
		trace.setData(
				dataset.getShape(), 
				dataset.getSlice(new int[]{0,0,0}, dataset.getShape(), step),
				intensityValue,
				opacityValue,
				minMaxValue,
				minMaxCulling);
		
		
		if ((IVolumeRenderTrace) system.getTrace(traceID) == null)
		{
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					system.addTrace(trace);
		    	}
		    });
		}
		
		return Status.OK_STATUS;
	}

	
	
}
