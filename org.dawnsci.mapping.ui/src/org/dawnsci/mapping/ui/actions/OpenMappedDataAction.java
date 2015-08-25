package org.dawnsci.mapping.ui.actions;

import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenMappedDataAction extends Action {

private static final Logger logger = LoggerFactory.getLogger(OpenMappedDataAction.class);
	
	private ISelectionProvider provider;

	/**
	 * Construct the OpenPropertyAction with the given page.
	 * 
	 * @param p
	 *            The page to use as context to open the editor.
	 * @param selectionProvider
	 *            The selection provider
	 */
	public OpenMappedDataAction(IWorkbenchPage p, ISelectionProvider selectionProvider) {
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
			if (sSelection.size() == 1 && sSelection.getFirstElement() instanceof IFile) {

				IFile file = (IFile)sSelection.getFirstElement();
				String loc = file.getRawLocation().toOSString();
				logger.debug(loc);
				
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart view = page.findView("org.dawnsci.mapping.ui.mappeddataview");
				if (view==null) return;
				
				final MappedFileManager manager = (MappedFileManager)view.getAdapter(MappedFileManager.class);
				if (manager != null) {
					manager.importFile(loc);
				}
				
			}
		}
		

	}
	
}
