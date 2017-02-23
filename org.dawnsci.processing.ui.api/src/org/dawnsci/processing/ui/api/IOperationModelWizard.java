package org.dawnsci.processing.ui.api;


import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

public interface IOperationModelWizard extends IWizard {
	public void saveOutputFile(String filename) throws Exception;

	public void setTemplateFile(String filename) throws Exception;
	
	@Override
	public IWizardPage[] getPages();
}
