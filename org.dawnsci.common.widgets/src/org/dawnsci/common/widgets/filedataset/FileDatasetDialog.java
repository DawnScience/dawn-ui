package org.dawnsci.common.widgets.filedataset;

import java.io.File;

import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class FileDatasetDialog extends Dialog {

	private final IFileDatasetFilter filter;
	private final File initialFile;
	private FileDatasetComposite composite;
	
	public FileDatasetDialog(Shell parentShell, IFileDatasetFilter filter) {
		this(parentShell, null, filter);
	}
	public FileDatasetDialog(Shell parentShell, File initialFile, IFileDatasetFilter filter) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.filter = filter;
		this.initialFile = initialFile;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		composite = new FileDatasetComposite(container, initialFile, filter, SWT.NONE);
		composite.addStatusCompositeChangedListener(event -> {
			boolean status = event.getStatus();
			getButton(IDialogConstants.OK_ID).setEnabled(status);
		});
		return container;
	}

	public File getSelectedFile() {
		return composite.getSelectedFile();
	}
	
	public ILazyDataset getSelectedDataset() {
		return composite.getSelectedDataset();
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
