package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.january.dataset.IDataset;

public interface PlottableMapObject extends MapObject {

	public String getLongName();
	
	public IDataset getData();
	
	public boolean isLive();
	
}
