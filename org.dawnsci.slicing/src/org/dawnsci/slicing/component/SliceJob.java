package org.dawnsci.slicing.component;

import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.slicing.api.system.ISliceSystem;
import org.dawnsci.slicing.api.system.SliceSource;
import org.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.SliceObject;

class SliceJob extends Job {
	
	private static final Logger logger = LoggerFactory.getLogger(SliceJob.class);
	 
	private SliceObject  slice;
	private Enum         sliceType;
	private ISliceSystem system;
	
	public SliceJob(ISliceSystem system) {
		super("Slice");
		this.system = system;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		if (slice==null) return Status.CANCEL_STATUS;
		monitor.beginTask("Slice "+slice.getName(), 10);
		try {
			monitor.worked(1);
			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		
			if (sliceType instanceof PlotType) {
				final SliceSource data = system.getData();
				// TODO FIXME Allow the current slice tool to dictate how to 
				// process the slice?
				SliceUtils.plotSlice(data.getLazySet(),
						             slice, 
						             data.getLazySet().getShape(), 
						             (PlotType)sliceType, 
						             system.getPlottingSystem(), 
						             monitor);
			}
		} catch (Exception e) {
			logger.error("Cannot slice "+slice.getName(), e);
			System.out.println(slice);
		} finally {
			monitor.done();
		}	
		
		return Status.OK_STATUS;
	}

	public void schedule(Enum sliceType, SliceObject cs, boolean force) {
		if (force==false && slice!=null && slice.equals(cs)) return;
		cancel();
		this.slice = cs;
		this.sliceType = sliceType;
		schedule();
	}	
}
