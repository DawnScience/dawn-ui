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
	@SuppressWarnings("unused")
	final static private Logger logger = LoggerFactory.getLogger(OperationModelWizard.class);
	
	public OperationModelWizard(final List<IOperationSetupWizardPage> wizardPages) {
		this.wizardPages = wizardPages;
		setHelpAvailable(false);
	}
	
	public OperationModelWizard(final IOperationSetupWizardPage wizardPage) {
		wizardPages = new ArrayList<>();
		wizardPages.add(wizardPage);
		setHelpAvailable(false);
	}
	
	@Override
	public void addPages() {
		wizardPages.stream().forEachOrdered(page -> addPage(page));
	}

	@Override
	public boolean performFinish() {
		wizardPages.stream().forEachOrdered(page -> {
			page.wizardTerminatingButtonPressed(Dialog.OK);
		});
		return true;
	}

	@Override
	public boolean performCancel() {
		wizardPages.stream().forEachOrdered(page -> {
			page.wizardTerminatingButtonPressed(Dialog.CANCEL);
		});
		return true;
	}
}
