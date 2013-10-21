package org.dawnsci.spectrum.ui.wizard;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.eclipse.jface.wizard.Wizard;

public class SpectrumWizard extends Wizard {
	
	IContain1DData data1;
	IContain1DData data2;
	
	public void add1DDatas(IContain1DData data1, IContain1DData data2){
		this.data1 = data1;
		this.data2 = data2;
		
	}

	
	@Override
	public void addPages(){
		
		SpectrumWizardPage wp = new SpectrumWizardPage();
		
		wp.add1DDatas(data1, data2);
		
		addPage(wp);
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}

}
