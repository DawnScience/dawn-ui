package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.IDataset;

public interface IPlotDataModifier {
	
	public void configure(IPlottingSystem<?> system);

	public IDataset modifyForDisplay(IDataset d);
	
	public void init();
	
	public boolean supportsRank(int rank);
	
	public String getName();
	
}
