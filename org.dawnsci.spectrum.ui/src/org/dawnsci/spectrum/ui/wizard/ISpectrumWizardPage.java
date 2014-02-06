package org.dawnsci.spectrum.ui.wizard;

import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;

public interface ISpectrumWizardPage {

	public void setDatasetList(List<IContain1DData> dataList);
	
	public void process();
	
	public List<IContain1DData> getOutputDatasetList();
	
}
