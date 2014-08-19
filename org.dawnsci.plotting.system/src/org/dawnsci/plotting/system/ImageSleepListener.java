package org.dawnsci.plotting.system;

import java.util.Collection;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.MultiPageEditorPart;

class ImageSleepListener implements IPartListener2 {

	private IPlottingSystem system;
	private IWorkbenchPart part;

	public ImageSleepListener(IPlottingSystem system,
			                   IWorkbenchPart part) {
		this.system = system;
		this.part   = part;
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		
		IWorkbenchPart thePart = partRef.getPart(false);
		if (this.part!=thePart && isMultiPage(part, thePart)) return;
		
		Collection<ITrace> traces = system.getTraces(IImageTrace.class);
		if (traces!=null && traces.size()>0) {
			IImageTrace trace = (IImageTrace)traces.iterator().next();
			trace.sleep();
		}
		
	}

	private boolean isMultiPage(IWorkbenchPart part, IWorkbenchPart thePart) {
		if (!(thePart instanceof MultiPageEditorPart)) return false;
		MultiPageEditorPart mpart = (MultiPageEditorPart)thePart;
		return part == mpart.getSelectedPage();
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

}
