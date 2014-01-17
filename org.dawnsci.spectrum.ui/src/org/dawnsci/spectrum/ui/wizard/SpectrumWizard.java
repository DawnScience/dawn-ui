package org.dawnsci.spectrum.ui.wizard;

import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.eclipse.jface.wizard.Wizard;

public class SpectrumWizard extends Wizard {
	
	List<IContain1DData> dataList;
	SaveFileWizardPage wp;
	
	public SpectrumWizard() {
	}
	
	public void add1DDatas(List<IContain1DData> dataList){
		this.dataList = dataList;
	}
	
	@Override
	public void addPages(){
		
		wp = new SaveFileWizardPage(dataList);
		
		//SpectrumSubtractionWizardPage wp = new SpectrumSubtractionWizardPage(dataList);
		addPage(wp);
	}

	@Override
	public boolean performFinish() {
		wp.finish();
		return true;
	}

}
