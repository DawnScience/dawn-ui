package org.dawnsci.processing.ui.model;

import java.util.Arrays;

import org.dawnsci.processing.ui.api.IOperationModelWizard;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationModelWizardDialog extends WizardDialog {

	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory.getLogger(OperationModelWizardDialog.class);
	
	public OperationModelWizardDialog(final Shell parentShell, final IOperationModelWizard wizard) {
		super(parentShell, wizard);
		addPageChangingListener(new IPageChangingListener() {
			
			@Override
			public void handlePageChanging(PageChangingEvent event) {
				IOperationSetupWizardPage currentPage = (IOperationSetupWizardPage) event.getCurrentPage();
				int currentIndex = Arrays.asList(wizard.getPages()).indexOf(currentPage);
				IOperationSetupWizardPage nextPage = (IOperationSetupWizardPage) event.getTargetPage();
				int nextIndex = Arrays.asList(wizard.getPages()).indexOf(nextPage);
				if (nextIndex <= currentIndex) {
					return; // nothing to do when going backwards...
				}
				nextPage.setInputData(currentPage.getOutputData());
				update();
			}
		});
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

	@Override
	protected void nextPressed() {
		IOperationSetupWizardPage currentPage = (IOperationSetupWizardPage) getCurrentPage();
		currentPage.finishPage();
		super.nextPressed();
	}
}
