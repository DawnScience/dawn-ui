package org.dawnsci.processing.ui.api;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.jface.wizard.IWizardPage;

public interface IOperationSetupWizardPage extends IWizardPage {
	
	/** Called whenever the Cancel or Finish button is clicked, or when the wizard dialog is closed
	 *  
	 * @param buttonId an integer identifying which button was pressed. Currently Dialog.OK ("Finish") and Dialog.CANCEL ("Cancel" or "Close") can be expected as value.
	 */
	public void wizardTerminatingButtonPressed(int buttonId);
	
	public OperationData getOutputData();
	
	public void setInputData(OperationData od);
	
	public void finishPage();
	
	public default boolean shouldSkipRemainingPages() {
		return false;
	}
	
}
