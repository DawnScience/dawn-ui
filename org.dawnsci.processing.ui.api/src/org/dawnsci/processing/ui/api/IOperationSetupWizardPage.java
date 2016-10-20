package org.dawnsci.processing.ui.api;

import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.jface.wizard.IWizardPage;

public interface IOperationSetupWizardPage extends IWizardPage {
	
	public void wizardButtonPressed(int buttonId);
	
	public void setOperationInputData(final IOperationInputData data);
}
