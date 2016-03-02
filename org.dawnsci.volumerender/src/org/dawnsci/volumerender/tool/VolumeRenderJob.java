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
 	final private IPlottingSystem system;
 	private String traceID;
 	private ILazyDataset dataset;

 	private double resolution;
 	private double opacity;
 	
	public VolumeRenderJob(String name, IPlottingSystem system) 
	{
		super(name);
		
		this.system = system;
	}
	
	public void compute(String traceID, double resolution, double opacity, ILazyDataset dataset) // add values
	{	
		this.traceID = traceID;
		this.resolution = resolution / 100;
		this.opacity = opacity / 100;
		this.dataset = dataset;
		
		destroy(traceID);
		
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
	
	@Override
	protected IStatus run(IProgressMonitor monitor) 
	{
		Thread.currentThread().setName("generating volume render");
		
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
				opacity);
		
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
