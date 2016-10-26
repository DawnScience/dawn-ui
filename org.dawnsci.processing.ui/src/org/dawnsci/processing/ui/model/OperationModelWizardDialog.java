package org.dawnsci.processing.ui.model;

import java.io.File;

import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationModelWizardDialog extends WizardDialog {

	private final OperationData initialData;
	private final static Logger logger = LoggerFactory.getLogger(OperationModelWizardDialog.class);
	
	public OperationModelWizardDialog(final Shell parentShell, final OperationModelWizard newWizard, final OperationData initialData) {
		super(parentShell, newWizard);
		this.initialData = initialData;
		newWizard.setInitialData(initialData);
		addPageChangingListener(new IPageChangingListener() {
			
			@Override
			public void handlePageChanging(PageChangingEvent event) {
				logger.debug("Changing wizard page");
				IOperationSetupWizardPage currentPage = (IOperationSetupWizardPage) event.getCurrentPage();
				int currentIndex = newWizard.wizardPages.indexOf(currentPage);
				IOperationSetupWizardPage nextPage = (IOperationSetupWizardPage) event.getTargetPage();
				int nextIndex = newWizard.wizardPages.indexOf(nextPage);
				if (nextIndex <= currentIndex) {
					logger.debug("Going to the previous page...");
					return; // nothing to do when going back...
				}
				IOperationInputData nextInputData = nextPage.getOperationInputData();
				nextInputData.setInputData(currentPage.getOperationData().getData());
				nextPage.setOperationInputData(nextInputData);
				nextPage.setOperationData(currentPage.getOperationData());
				nextPage.update();
				//nextPage.setVisible(true);
				//nextPage.getControl().redraw();
				update();
				logger.debug("Going to the next page...");
			}
		});
		addPageChangedListener(new IPageChangedListener() {
			
			@Override
			public void pageChanged(PageChangedEvent event) {
				IWizardPage page = (IWizardPage) event.getSelectedPage();
				logger.debug("Page changed to {}", page.getName());
				//page.setVisible(true);
				Control control = page.getControl();
				logger.debug("isVisible {}", control.isVisible());
				@SuppressWarnings("unused")
				int temp = 5+5;
			}
		});
	}

	public OperationModelWizardDialog(final Shell parentShell, final OperationModelWizard newWizard, final File inputFile, final String datasetName) {
		this(parentShell, newWizard, null);
		// TODO: read dataset from file
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
	
	public OperationData getInitialData() {
		return initialData;
	}
	
}
