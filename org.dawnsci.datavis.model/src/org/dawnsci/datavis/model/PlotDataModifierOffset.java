package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

public class PlotDataModifierOffset implements IPlotDataModifier {

private double value = 0;
	
	@Override
	public IDataset modifyForDisplay(IDataset d) {
		double min = d.min(true).doubleValue();
		double max = d.max(true).doubleValue();
		Dataset dataset = DatasetUtils.convertToDataset(d);
		dataset.iadd(value-min);
		value = dataset.max(true).doubleValue();
		return d;
	}

	@Override
	public void init() {
		value = 0;
	}

	@Override
	public int getSupportedRank() {
		return 1;
	}

	@Override
	public String getName() {
		return "Offset";
	}

}
