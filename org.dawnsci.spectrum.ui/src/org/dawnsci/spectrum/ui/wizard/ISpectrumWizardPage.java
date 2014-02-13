package org.dawnsci.spectrum.ui.wizard;

import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;

public interface ISpectrumWizardPage {

	
	public List<IContain1DData> process(List<IContain1DData> dataList);
	
}
