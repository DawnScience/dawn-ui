package org.dawnsci.spectrum.ui.wizard;

import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.file.ISpectrumFile;
import org.dawnsci.spectrum.ui.file.SpectrumInMemory;
import org.dawnsci.spectrum.ui.processing.AbstractProcess;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

public class SpectrumWizard extends Wizard {
	
	String outputPath = null;
	List<IContain1DData> outdata = null;
	List<IContain1DData> data;
	
	public SpectrumWizard() {
	}
	
	public void setData(List<IContain1DData> data) {
		this.data = data;
	}
	
	@Override
	public boolean performFinish() {
		IWizardPage page = getPages()[0];
		
		if (page instanceof ISpectrumWizardPage) { 
			outdata = ((ISpectrumWizardPage)page).process(data);
		}
		
		if (page instanceof SaveFileWizardPage) outputPath = ((SaveFileWizardPage)page).getAbsoluteFilePath();
		
		return true;
	}

	public String getOutputPath() {
		return outputPath;
	}
	
	public List<ISpectrumFile> getOutputFiles() {
		for (IContain1DData data : outdata) {
			//SpectrumInMemory sm = new SpectrumInMemory(longName, name, xDataset, yDatasets, system);
		}
		return null;
	}
	
}
