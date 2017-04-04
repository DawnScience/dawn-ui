package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

public class PlotDataModifierStack implements IPlotDataModifier {

	private double value = 0;
	
	@Override
	public IDataset modifyForDisplay(IDataset d) {
		double min = d.min(true).doubleValue();
		double max = d.max().doubleValue();
		Dataset dataset = DatasetUtils.convertToDataset(d);
		dataset.isubtract(min).idivide(max-min);
		dataset.iadd(value*0.2);
		value++;
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
		return "Stack";
	}

}
