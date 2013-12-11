package org.dawnsci.spectrum.ui.actions;

import java.io.File;

import org.dawnsci.spectrum.ui.file.SpectrumFileManager;
import org.dawnsci.spectrum.ui.views.TraceProcessPage;
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

	@Override
	public void run(IAction action) {
		run();
	}
	
	@Override
	public void run() {
		FileDialog dialog =  new FileDialog(window.getShell(), SWT.OPEN | SWT.MULTI);
		dialog.setText("Open file");
		//dialog.setFilterPath(filterPath);
		dialog.open();
		String[] names =  dialog.getFileNames();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view = page.findView("org.dawnsci.spectrum.ui.views.SpectrumView");
		if (view==null) return;
		
		final SpectrumFileManager manager = (SpectrumFileManager)view.getAdapter(SpectrumFileManager.class);
		if (manager != null) {
			for (String name : names) {
				manager.addFile(dialog.getFilterPath() + File.separator + name);
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
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window =  window;
	}

}
