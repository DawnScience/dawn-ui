package org.dawnsci.spectrum.ui.file;

import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public interface IContain1DData {
	
	public IDataset getxDataset();
	
	public List<IDataset> getyDatasets();
	
	public String getName();
	
	public String getLongName();

}
