package org.dawnsci.processing.ui.model;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class OperationModelWizardDialog extends WizardDialog {

	public OperationModelWizardDialog(Shell parentShell, OperationModelWizard newWizard) {
		super(parentShell, newWizard);
	}

	@Override
	protected Point getInitialSize() {
		Rectangle bounds = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		return new Point((int)(bounds.width*0.8),(int)(bounds.height*0.8));
	}
	
	@Override
	protected boolean isResizable() {
	  return true;
	}
	
	@Override
	protected void handleShellCloseEvent() {
		getWizard().performCancel();
		super.handleShellCloseEvent();
	}
}
