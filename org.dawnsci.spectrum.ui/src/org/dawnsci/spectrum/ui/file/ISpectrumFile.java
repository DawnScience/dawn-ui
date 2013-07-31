package org.dawnsci.spectrum.ui.file;

import java.util.Collection;
import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public interface ISpectrumFile {

	public String getName();
	
	public Collection<String> getDataNames();
	
	public IDataset getDataset(String name);
	
	public IDataset getxDataset();
	
	public List<IDataset> getyDatasets();
	
	public List<String> getyDatasetNames();
	
	public String getxDatasetName();
	
	public boolean contains(String datasetName);
	
	public void removeyDatasetName(String name);
	
	public void addyDatasetName(String name);
	
	public void plotAll();
	
	public void removeAllFromPlot();
	
	public String getPath();
}
