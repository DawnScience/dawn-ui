package org.dawnsci.mapping.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

public class ImportMappedDataWizard extends Wizard {

	private String filePath;
	
	public ImportMappedDataWizard(String filePath) {
		this.filePath = filePath;
		addPage(new ImportDataCubeWizardPage(this.filePath));
	}
	
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}
