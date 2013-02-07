package org.dawnsci.plotting.tools.masking;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class MaskExportWizard extends Wizard implements IExportWizard {

	public MaskExportWizard() {
		
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
        addPage(new WizardNewFileCreationPage("Export Location", selection));
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}
