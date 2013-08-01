package org.dawnsci.spectrum.ui.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.ITrace;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;


public class SpectrumInMemory implements ISpectrumFile {
	
	private String xDatasetName;
	private IPlottingSystem system;
	private List<String> yDatasetNames;
	Map<String,IDataset> datasets;
	private String name;
	
	public SpectrumInMemory(String name,IDataset xDataset ,Collection<IDataset> yDatasets, IPlottingSystem system) {
		this.system = system;
		this.name = name;
		datasets = new HashMap<String, IDataset>();
		yDatasetNames = new ArrayList<String>(yDatasets.size());
		
		if (xDataset != null) {
			String dsName = xDataset.getName();
			if (dsName == null) dsName = "xData";
			datasets.put(dsName, xDataset);
			xDatasetName = dsName;
		}
		
		//TODO make more robust to the same dataset names
		int i = 0;
		for (IDataset dataset : yDatasets) {
			String dsName = dataset.getName();
			if (dsName == null) dsName = "yData" + i;
			datasets.put(dsName, dataset);
			yDatasetNames.add(dsName);
		}
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	
	
	@Override
	public Collection<String> getDataNames() {
		
		Collection<String> col = new ArrayList<String>(datasets.size());
		
		for (String key : datasets.keySet()) {
			col.add(key);
		}
		
		return col;
	}
	
	@Override
	public IDataset getDataset(String name) {
		return datasets.get(name);
	}

	@Override
	public IDataset getxDataset() {
		return datasets.get(xDatasetName);
	}

	@Override
	public List<IDataset> getyDatasets() {
		List<IDataset> sets = new ArrayList<IDataset>();
		for (String name : yDatasetNames) {
			sets.add(datasets.get(name));
		}
		return sets;
	}

	@Override
	public List<String> getyDatasetNames() {
		return yDatasetNames;
	}

	@Override
	public String getxDatasetName() {
		// TODO Auto-generated method stub
		return xDatasetName;
	}

	@Override
	public boolean contains(String datasetName) {
		for (String name : datasets.keySet()) {
			if (datasetName.equals(name)) return true;
		}
		
		return false;
	}
	

	@Override
	public void removeyDatasetName(String name) {
		yDatasetNames.remove(name);
		removeFromPlot(name);
		
	}

	@Override
	public void addyDatasetName(String name) {
		//TODO check contains before adding removing
		yDatasetNames.add(name);
		addToPlot(name);
	}
	
	public void plotAll() {
		system.updatePlot1D(getxDataset(), getyDatasets(), null);
	}

	private void removeFromPlot(String name) {
		ITrace trace = system.getTrace(name);
		if (trace != null) system.removeTrace(trace);
		system.autoscaleAxes();
	}
	
	private void addToPlot(String name) {
		IDataset set = datasets.get(name);
		
		if (set != null) system.updatePlot1D(getxDataset(), Arrays.asList(new IDataset[] {set}), null);
	}
	
	public void removeAllFromPlot() {
		for (String dataset : getyDatasetNames()) {
			ITrace trace = system.getTrace(dataset);
			if (trace != null) system.removeTrace(trace);
		}
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return name;
	}

}
