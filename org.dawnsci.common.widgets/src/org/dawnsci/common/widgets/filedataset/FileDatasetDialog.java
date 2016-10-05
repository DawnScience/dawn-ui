package org.dawnsci.common.widgets.filedataset;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/*
 * After http://blog.vogella.com/2009/06/23/eclipse-rcp-file-browser/
 */

public class FileDatasetDialog extends Dialog {

	public FileDatasetDialog(Shell parentShell) {
		super(parentShell);
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		// add a sash with a treeviewer and a tableviewer
		Composite container = (Composite) super.createDialogArea(parent);
		SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		TreeViewer treeViewer = new TreeViewer(sashForm, SWT.NONE);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableViewer tableViewer = new TableViewer(sashForm, SWT.NONE);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.setWeights(new int[]{50, 50});
		
		return container;
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
}
