package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.AxesMetadata;

public class PlotDataModifierOffset implements IPlotDataModifier {

private double value = 0;
	
	@Override
	public IDataset modifyForDisplay(IDataset d) {
		double min = d.min(true).doubleValue();
		Dataset dataset = DatasetUtils.convertToDataset(d);
		dataset = Maths.add(dataset,(value-min));
		value = dataset.max(true).doubleValue();
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
		return "Offset";
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
