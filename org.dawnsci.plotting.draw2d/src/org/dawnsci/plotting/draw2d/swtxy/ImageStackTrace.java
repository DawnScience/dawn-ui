package org.dawnsci.plotting.draw2d.swtxy;

import java.util.Collection;
import java.util.HashSet;

import org.csstudio.swt.widgets.figureparts.ColorMapRamp;
import org.csstudio.swt.xygraph.figures.Axis;
import org.dawnsci.plotting.api.trace.IImageStackTrace;
import org.dawnsci.plotting.api.trace.IStackPositionListener;
import org.dawnsci.plotting.api.trace.StackPositionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;

public class ImageStackTrace extends ImageTrace implements IImageStackTrace {

	private int          index=0;
	private ILazyDataset stack;
    private StackJob     stackJob;
    
	public ImageStackTrace(String       name, 
			               Axis         xAxis, 
			               Axis         yAxis,
			               ColorMapRamp intensityScale) {
		super(name, xAxis, yAxis, intensityScale);
		this.stackJob = new StackJob();
	}

	@Override
	public void setStack(ILazyDataset stack) {
		this.stack = stack;
	}

	@Override
	public int getStackSize() {
		return stack.getShape()[0];
	}

	@Override
	public int getStackIndex() {
		return index;
	}

	@Override
	public void setStackIndex(int index) {
		
		this.index = index;
		if (isActive()) {
		    stackJob.scheduleSlice(index);
		} else {
			IDataset set = stack.getSlice(new int[]{index,0,0}, 
										  new int[]{index+1,stack.getShape()[1], stack.getShape()[2]},
										  new int[]{1,1,1});
			set = (IDataset)set.squeeze();
			setData((AbstractDataset)set, getAxes(), false);
			
		}
	}
	
	private class StackJob extends Job {
		
		private int index;

		StackJob() {
			super("Stack slice");
			setPriority(Job.INTERACTIVE);
			setUser(false);
			setSystem(true);
		}

		public void scheduleSlice(int index) {
			cancel();
			this.index = index;
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			try {
				IDataset set = stack.getSlice(new int[]{index,0,0}, 
											  new int[]{index+1,stack.getShape()[1], stack.getShape()[2]},
											  new int[]{1,1,1});
				set = (IDataset)set.squeeze();
				final AbstractDataset absData = (AbstractDataset)set;
				
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						setData(absData, getAxes(), false);
						fireStackPositionListeners(index);
					}
				});
				return Status.OK_STATUS;
			} catch (Throwable ne) {
				return Status.OK_STATUS;
			}
		}
	}

	private void fireStackPositionListeners(int i) {
		if (listeners==null) return;
		final StackPositionEvent evt = new StackPositionEvent(this, i);
		for (IStackPositionListener l : listeners) {
			try {
				l.stackPositionChanged(evt);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private Collection<IStackPositionListener> listeners;
	@Override
	public void addStackPositionListener(IStackPositionListener l) {
		if (listeners==null) listeners = new HashSet<IStackPositionListener>(7);
		listeners.add(l);
	}

	@Override
	public void removeStackPositionListener(IStackPositionListener l) {
		if (listeners==null) return;
		listeners.remove(l);
	}
	
	public void remove() {
        super.remove();
        if (listeners!=null) listeners.clear();
        listeners = null;
	}
}
