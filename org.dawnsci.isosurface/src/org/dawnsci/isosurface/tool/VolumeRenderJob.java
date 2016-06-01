package org.dawnsci.isosurface.tool;

import org.dawnsci.isosurface.Activator;

import org.dawnsci.isosurface.alg.VolumeRenderer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.slicing.api.util.ProgressMonitorWrapper;

public class VolumeRenderJob extends Job {	
	private VolumeRenderer volumeRenderer;

	public VolumeRenderJob(String name) {
		super(name);
	}

	public void compute(VolumeRenderer volumeRenderer)	{	
		this.volumeRenderer = volumeRenderer;
		
		cancel();
		schedule();
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			volumeRenderer.run(new ProgressMonitorWrapper(monitor));
		} catch (Exception e) {
			return new Status(Status.ERROR, Activator.PLUGIN_ID, "Failed to render volume", e);
		}
		return Status.OK_STATUS;
	}
}
