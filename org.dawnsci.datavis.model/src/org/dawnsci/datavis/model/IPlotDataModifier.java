package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.IDataset;

public interface IPlotDataModifier {

	public IDataset modifyForDisplay(IDataset d);
	
	public void init();
	
	public int getSupportedRank();
	
	public String getName();
	
}
