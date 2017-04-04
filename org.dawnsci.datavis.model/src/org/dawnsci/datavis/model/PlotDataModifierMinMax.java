package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

public class PlotDataModifierMinMax implements IPlotDataModifier {

	@Override
	public IDataset modifyForDisplay(IDataset d) {
		double min = d.min(true).doubleValue();
		double max = d.max().doubleValue();
		Dataset dataset = DatasetUtils.convertToDataset(d);
		dataset.isubtract(min).idivide(max-min);
		
		return dataset;
	}

	@Override
	public int getSupportedRank() {
		return 1;
	}

	@Override
	public String getName() {
		return "Min-Max";
	}

	@Override
	public void init() {
		//no init required
	}

}
