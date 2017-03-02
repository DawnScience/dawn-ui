package org.dawnsci.datavis.api;

import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public interface IDataPackage {

	public boolean isSelected();
	
	public String getFilePath();
	
	public String getName();
	
	public SliceND getSlice();
	
	public ILazyDataset getLazyDataset();
}
