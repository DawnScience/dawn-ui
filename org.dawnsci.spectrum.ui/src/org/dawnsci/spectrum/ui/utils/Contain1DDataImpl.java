package org.dawnsci.spectrum.ui.utils;

import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public class Contain1DDataImpl implements IContain1DData {
	
	private IDataset x;
	private List<IDataset> y;
	
	public Contain1DDataImpl(IDataset x, List<IDataset> y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public IDataset getxDataset() {
		return x;
	}

	@Override
	public List<IDataset> getyDatasets() {
		return y;
	}

}
