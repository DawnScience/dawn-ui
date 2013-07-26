package org.dawnsci.spectrum.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class SpectrumFile {
	
	private String path;
	private IMetaData meta;
	private Collection<String> dataNames;
	private String xDatasetName;
	
	private List<String> yDatasetNames;
	
	private static Logger logger = LoggerFactory.getLogger(SpectrumFile.class);
	
	public SpectrumFile(String path, IMetaData meta, Collection<String > dataNames) {
		this.path = path;
		this.meta = meta;
		this.dataNames = dataNames;
		this.yDatasetNames = new ArrayList<String>();
	}
	
	public String getPath() {
		return path;
	}
	
	public Collection<String> getDataNames() {
		return dataNames;
	}
	
	public boolean contains(String datasetName) {
		for (String name : dataNames) {
			if (datasetName.equals(name)) return true;
		}
		
		return false;
	}
	
	public String getxDatasetName() {
		return xDatasetName;
	}

	public void setxDatasetName(String xDatasetName) {
		this.xDatasetName = xDatasetName;
	}
	
	public List<String> getyDatasetNames() {
		return yDatasetNames;
	}
	
	public void addyDatasetName(String name) {
		//TODO check contains before adding removing
		yDatasetNames.add(name);
	}
	
	public void removeyDatasetName(String name) {
		//TODO check contains before adding removing
		yDatasetNames.remove(name);
	}
	
	public IDataset getxDataset() {
		try {
			IDataset x =  LoaderFactory.getDataSet(path, xDatasetName, null);
			if (x == null) return null;
			x.setName(path + " : " + xDatasetName);
			return x;
		} catch (Exception e) {
			return null;
		}
	}
	
	public List<IDataset> getyDatasets() {
		List<IDataset> sets = new ArrayList<IDataset>();
		
		for (String name : yDatasetNames) {
			try {
				IDataset set = LoaderFactory.getDataSet(path, name, null);
				if (set != null) {
					set.setName(path + " : " + name);
					sets.add(set);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		
		return sets;
	}
	
}

