package org.dawnsci.processing.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationModelWizard extends Wizard {

	final List<IOperationSetupWizardPage> wizardPages;
	final static private Logger logger = LoggerFactory.getLogger(OperationModelWizard.class);
	
	public OperationModelWizard(final List<IOperationSetupWizardPage> wizardPages) {
		this.wizardPages = wizardPages;
	}
	
	public OperationModelWizard(final IOperationSetupWizardPage wizardPage) {
		wizardPages = new ArrayList<>();
		wizardPages.add(wizardPage);
	}
	
	@Override
	public void addPages() {
		wizardPages.stream().forEachOrdered(page -> addPage(page));
	}

	@Override
	public boolean performFinish() {
		logger.debug("OperationModelWizard performFinish clicked");
		wizardPages.stream().forEachOrdered(page -> {
			page.wizardButtonPressed(Dialog.OK);
		});
		return true;
	}

	@Override
	public boolean performCancel() {
		logger.debug("OperationModelWizard performCancel clicked");
		wizardPages.stream().forEachOrdered(page -> {
			page.wizardButtonPressed(Dialog.CANCEL);
		});
		return true;
	}
}
