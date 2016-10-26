package org.dawnsci.processing.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationModelWizard extends Wizard {

	final List<IOperationSetupWizardPage> wizardPages;
	@SuppressWarnings("unused")
	private OperationData initialData;
	
	@SuppressWarnings("unused")
	final static private Logger logger = LoggerFactory.getLogger(OperationModelWizard.class);
	
	public OperationModelWizard(final List<IOperationSetupWizardPage> wizardPages) {
		if (wizardPages == null || wizardPages.size()== 0)
			throw new IllegalArgumentException("Constructor must be passed at least one IOperationSetupWizardPage");
		this.wizardPages = wizardPages;
		setHelpAvailable(false);
	}
	
	public OperationModelWizard(final IOperationSetupWizardPage wizardPage) {
		if (wizardPage == null)
			throw new IllegalArgumentException("Constructor must be passed at least one IOperationSetupWizardPage");
		wizardPages = new ArrayList<>();
		wizardPages.add(wizardPage);
		setHelpAvailable(false);
	}
	
	public OperationModelWizard(final IOperationSetupWizardPage... wizardPages) {
		if (wizardPages == null || wizardPages.length == 0)
			throw new IllegalArgumentException("Constructor must be passed at least one IOperationSetupWizardPage");
		this.wizardPages = new ArrayList<>();
		Arrays.stream(wizardPages).forEachOrdered(wizardPage -> this.wizardPages.add(wizardPage));
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

	public void setInitialData(OperationData initialData) {
		this.initialData = initialData;
		wizardPages.get(0).setOperationData(initialData);
	}
}
