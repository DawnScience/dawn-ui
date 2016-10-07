package org.dawnsci.common.widgets.filedataset;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class FileDatasetDialog extends Dialog {

	private final IFileDatasetFilter filter;
	
	public FileDatasetDialog(Shell parentShell, IFileDatasetFilter filter) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.filter = filter;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		FileDatasetComposite composite = new FileDatasetComposite(container, filter, SWT.NONE);
		composite.addFileDatasetCompositeStatusChangedListener(new IFileDatasetCompositeStatusChangedListener() {
			@Override
			public void compositeStatusChanged(FileDatasetCompositeStatusChangedEvent event) {
				boolean status = event.getStatus();
				getButton(IDialogConstants.OK_ID).setEnabled(status);
			}
		});
		return container;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select a dataset from a file");
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control buttonBar = super.createButtonBar(parent);
		// ensure ok button is disabled on startup
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return buttonBar;
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
}
