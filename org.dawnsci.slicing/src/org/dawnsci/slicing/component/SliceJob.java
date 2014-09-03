package org.dawnsci.slicing.component;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;

class SliceJob extends Job {
	
	private static final Logger logger = LoggerFactory.getLogger(SliceJob.class);
	 
	private SliceObject  slice;
	private Enum         sliceType;
	private ISliceSystem system;
	
	public SliceJob(ISliceSystem system) {
		super("Slice");
		this.system = system;
		setPriority(INTERACTIVE);
		setUser(false); // Shows a job in the bottom right but not in a dialog.
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		try {
			if (slice==null) return Status.CANCEL_STATUS;
			monitor.beginTask("Slice "+slice.getName(), 10);
			monitor.worked(1);
			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		
			if (system.getActiveTool()!=null && !system.getActiveTool().isSliceRequired()) {
				return Status.CANCEL_STATUS;
			}

			if (sliceType instanceof PlotType) {
				final SliceSource data = system.getData();
				IDataset slicedData = SliceUtils.plotSlice(data,
															slice, 
															(PlotType)sliceType, 
															system.getPlottingSystem(), 
															monitor);

				system.setSliceMetadata(slicedData!=null ? slicedData.getMetadata() : null);
			}

		} catch (Exception e) {
			logger.error("Cannot slice "+slice.getName(), e);
			System.out.println(slice);
		} finally {
			
			if (!system.isEnabled()) Display.getDefault().syncExec(new Runnable() {
				public void run() {
					system.setEnabled(true);
				}
			});

			monitor.done();
		}	
		
		return Status.OK_STATUS;
	}

	public void schedule(Enum sliceType, SliceObject cs, boolean force) {
		if (force==false && slice!=null && slice.equals(cs)) return;
		// DO NOT: cancel();
		this.slice          = cs;
		this.sliceType      = sliceType;
		schedule();
	}	
}
