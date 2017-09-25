package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.AxesMetadata;

public class PlotDataModifierStack implements IPlotDataModifier {

	private double value = 0;
	private double proportion = 0.2;
	
	@Override
	public IDataset modifyForDisplay(IDataset d) {
		double min = d.min(true).doubleValue();
		double max = d.max().doubleValue();
		double delta = max-min;
		if (delta == 0) delta = 1;
		Dataset dataset = DatasetUtils.convertToDataset(d);
		dataset = Maths.subtract(dataset, min).idivide(delta);
		dataset.iadd(value*proportion);
		value++;
		
		AxesMetadata md = d.getFirstMetadata(AxesMetadata.class);
		if (md != null) {
			dataset.addMetadata(md);
		}
		
		return dataset;
	}

	@Override
	public void init() {
		value = 0;
	}

	@Override
	public String getName() {
		return "Stack";
	}

	@Override
	public boolean supportsRank(int rank) {
		return rank == 1;
	}

	public double getProportion() {
		return proportion;
	}

	public void setProportion(double proportion) {
		this.proportion = proportion;
	}

}
