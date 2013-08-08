package org.dawnsci.spectrum.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.spectrum.ui.file.ISpectrumFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class SpectrumFile implements ISpectrumFile {
	
	private String path;
	private IMetaData meta;
	private Collection<String> dataNames;
	private String xDatasetName;
	private IPlottingSystem system;
	private List<String> yDatasetNames;
	private boolean hasXAxis = false;
	
	private static Logger logger = LoggerFactory.getLogger(SpectrumFile.class);
	
	public SpectrumFile(String path, IMetaData meta, Collection<String > dataNames, IPlottingSystem system) {
		this.path = path;
		this.meta = meta;
		this.dataNames = dataNames;
		this.yDatasetNames = new ArrayList<String>();
		this.system = system;
	}
	
	public String getName() {
		File file = new File(path);
		return file.getName();
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
		hasXAxis = true;
		this.xDatasetName = xDatasetName;
	}
	
	public List<String> getyDatasetNames() {
		return yDatasetNames;
	}
	
	public void addyDatasetName(String name) {
		//TODO check contains before adding removing
		yDatasetNames.add(name);
		addToPlot(name);
	}
	
	public void removeyDatasetName(String name) {
		//TODO check contains before adding removing
		yDatasetNames.remove(name);
		removeFromPlot(name);
	}
	
	public IDataset getxDataset() {
		return getDataset(xDatasetName);
	}
	
	public IDataset getDataset(String name) {
		try {
			IDataset x =  LoaderFactory.getDataSet(path, name, null);
			if (x == null) return null;
			x.setName(name);
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
					set.setName(name);
					sets.add(set);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		
		return sets;
	}
	
	public boolean hasXAxis() {
		return hasXAxis;
	}
	
	public void useXAxis(boolean useX) {
		if (xDatasetName != null && useX) hasXAxis = true;
		else hasXAxis = false;
	}
	
	public void plotAll() {
		
		List<IDataset> list = getyDatasets();
		for (IDataset ds : list) ds.setName(path + " : " + ds.getName());
		
		system.updatePlot1D(getxDataset(), list, null);
	}
	
	public void removeAllFromPlot() {
		for (String dataset : getyDatasetNames()) {
			ITrace trace = system.getTrace(getPath() + " : " + dataset);
			if (trace != null) system.removeTrace(trace);
		}
	}
	
	private void addToPlot(String name) {
		IDataset set = null;
		try {
			set = LoaderFactory.getDataSet(path, name, null);
			if (set != null) {
				set.setName(path + " : " + name);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		if (set != null) system.updatePlot1D(getxDataset(), Arrays.asList(new IDataset[] {set}), null);
	}
	
	private void removeFromPlot(String name) {
		ITrace trace = system.getTrace(getPath() + " : " + name);
		if (trace != null) system.removeTrace(trace);
		system.autoscaleAxes();
	}
	
}

