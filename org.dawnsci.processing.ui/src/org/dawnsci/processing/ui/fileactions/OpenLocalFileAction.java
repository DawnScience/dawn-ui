package org.dawnsci.processing.ui.fileactions;

import java.io.File;

import org.dawnsci.processing.ui.slice.FileManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class OpenLocalFileAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private String filterPath;

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

			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView("org.dawnsci.processing.ui.DataFileSliceView");
			if (view==null) return;

			final FileManager manager = (FileManager)view.getAdapter(FileManager.class);
			if (manager != null) {
				for (String name : names) {
					manager.addFiles(new String[]{dialog.getFilterPath() + File.separator + name});
				}
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
