package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

public interface PlottableMapObject extends MapObject {

	public String getLongName();
	
	public IDataset getData();
	
}
