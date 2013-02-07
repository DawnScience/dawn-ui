package org.dawnsci.plotting.tools.masking;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class MaskImportWizard extends Wizard implements IImportWizard {

	public static final String ID = "org.dawnsci.plotting.importMask";

	public MaskImportWizard() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}
