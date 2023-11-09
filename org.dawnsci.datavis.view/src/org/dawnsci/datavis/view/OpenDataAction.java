package org.dawnsci.datavis.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.plotting.api.PlottingEventConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class OpenDataAction extends Action {
	
	private ISelectionProvider provider;

	/**
	 * Construct the OpenPropertyAction with the given page.
	 * 
	 * @param p
	 *            The page to use as context to open the editor.
	 * @param selectionProvider
	 *            The selection provider
	 */
	public OpenDataAction(IWorkbenchPage p, ISelectionProvider selectionProvider) {
		setText("Open Property"); //$NON-NLS-1$
		provider = selectionProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		ISelection selection = provider.getSelection();
		if (!selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1 && sSelection.getFirstElement() instanceof IFile) {

				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		ISelection selection = provider.getSelection();
		if (!selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1 && sSelection.getFirstElement() instanceof IFile file) {
				String loc = file.getRawLocation().toOSString();

				final EventAdmin admin = ServiceProvider.getService(EventAdmin.class);
				Map<String,String[]> props = new HashMap<>();
				props.put(PlottingEventConstants.MULTIPLE_FILE_PROPERTY, new String[] {loc});
				admin.sendEvent(new Event(PlottingEventConstants.FILE_OPEN_EVENT, props));
			}
		}
	}

}
