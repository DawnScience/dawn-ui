package org.dawnsci.common.widgets.filedataset;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class FileDatasetDialog extends Dialog {

	public FileDatasetDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		new FileDatasetComposite(container, SWT.NONE);
		return container;
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
}
