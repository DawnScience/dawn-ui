package org.dawnsci.mapping.ui.actions;

import java.io.File;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class OpenLocalFileAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private String filterPath;
	protected boolean wildcard = false;

	@Override
	public void run(IAction action) {
		run();
	}
	
	@Override
	public void run() {
		FileDialog dialog =  new FileDialog(window.getShell(), SWT.OPEN | SWT.MULTI);
		dialog.setText("Open file");
		dialog.setFilterPath(filterPath);
		dialog.open();
		String[] names =  dialog.getFileNames();
		
		if (names != null) {
			filterPath =  dialog.getFilterPath();
			BundleContext bundleContext =
	                FrameworkUtil.
	                getBundle(this.getClass()).
	                getBundleContext();
			
			IMapFileController manager = bundleContext.getService(bundleContext.getServiceReference(IMapFileController.class));
			if (manager != null) {
				for (int i = 0; i < names.length; i++) {
					names[i] = dialog.getFilterPath() + File.separator +names[i];
				}
				
				manager.loadFiles(names, null);
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		window = null;
		filterPath = null;
	}
	
	@Override
	public void init(IWorkbenchWindow window) {
		this.window =  window;
		filterPath =  System.getProperty("user.home");
	}
}
