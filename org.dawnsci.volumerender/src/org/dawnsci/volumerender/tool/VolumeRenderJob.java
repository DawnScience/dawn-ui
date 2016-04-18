package org.dawnsci.volumerender.tool;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

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
		volumeRenderer.run();
		return Status.OK_STATUS;
	}
}
