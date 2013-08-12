package org.dawnsci.spectrum.ui.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.api.IPlottingSystem;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;


public class SpectrumInMemory extends AbstractSpectrumFile implements ISpectrumFile {
	
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
		
		return getDatasetNames();
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
	
	public void plotAll() {
		
		IDataset x = null;
		if (useAxisDataset) x = getxDataset();
		
		List<IDataset> list = getyDatasets();
		for (IDataset ds : list) {
			if (ds.getRank() != 1) ds = reduceTo1D(x, ds);
			ds.setName(this.name + " : " + ds.getName());
		}
		
		
		
		system.updatePlot1D(x, getyDatasets(), null);
	}
	
	protected void addToPlot(String name) {
		IDataset x = null;
		if (useAxisDataset) x = getxDataset();
		IDataset set = datasets.get(name);
		if (set.getRank() != 1) set = reduceTo1D(x, set);
		set.setName(this.name + " : " + set.getName());
		
		if (set != null) system.updatePlot1D(x, Arrays.asList(new IDataset[] {set}), null);
	}
	
	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public List<String> getPossibleAxisNames() {
		
		return getDatasetNames();
	}

	@Override
	public List<String> getMatchingDatasets(int size) {
		// TODO Auto-generated method stub
		return getDatasetNames();
	}
	
	private List<String> getDatasetNames() {
		List<String> col = new ArrayList<String>(datasets.size());
		
		for (String key : datasets.keySet()) {
			col.add(key);
		}
		
		return col;
	}
}
