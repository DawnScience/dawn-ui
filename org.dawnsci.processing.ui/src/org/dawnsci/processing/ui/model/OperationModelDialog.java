package org.dawnsci.processing.ui.model;

import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class OperationModelDialog extends Dialog {

	private OperationModelViewer modelViewer;

	public OperationModelDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.RESIZE|SWT.DIALOG_TRIM);
	}

	public Control createDialogArea(Composite parent) {
		
		this.modelViewer = new OperationModelViewer();
		modelViewer.createPartControl(parent);
		
		return parent;
	}
	
	public void setModel(IOperationModel model) {
		modelViewer.setModel(model);
	}
	public IOperationModel getModel() {
		return modelViewer.getModel();
	}

}
