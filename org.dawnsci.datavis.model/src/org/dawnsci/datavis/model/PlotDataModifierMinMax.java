package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.AxesMetadata;

public class PlotDataModifierMinMax implements IPlotDataModifier {

	@Override
	public IDataset modifyForDisplay(IDataset d) {
		double min = d.min(true).doubleValue();
		double max = d.max().doubleValue();
		double delta = max-min;
		if (delta == 0) delta = 1;
		Dataset dataset = DatasetUtils.convertToDataset(d);
		dataset = Maths.subtract(dataset,min).idivide(delta);
		AxesMetadata md = d.getFirstMetadata(AxesMetadata.class);
		if (md != null) {
			dataset.addMetadata(md);
		}
		
		return dataset;
	}

	@Override
	public String getName() {
		return "Min-Max";
	}

	@Override
	public void init() {
		//no init required
	}

	@Override
	public boolean supportsRank(int rank) {
		return rank == 1;
	}

	@Override
	public void configure(IPlottingSystem<?> system) {
		// TODO Auto-generated method stub
		
	}

}
