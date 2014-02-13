package org.dawnsci.spectrum.ui.utils;

import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public class Contain1DDataImpl implements IContain1DData {
	
	private IDataset x;
	private List<IDataset> y;
	private String name;
	private String longName;
	
	public Contain1DDataImpl(IDataset x, List<IDataset> y, String name, String longName) {
		this.x = x;
		this.y = y;
		this.name = name;
		this.longName = longName;
	}

	@Override
	public IDataset getxDataset() {
		return x;
	}

	@Override
	public List<IDataset> getyDatasets() {
		return y;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLongName() {
		return longName;
	}

}
