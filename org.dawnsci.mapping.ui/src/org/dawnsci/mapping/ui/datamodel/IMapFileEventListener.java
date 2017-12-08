package org.dawnsci.mapping.ui.datamodel;

import java.util.EventListener;

public interface IMapFileEventListener extends EventListener {
	
	public void mapFileStateChanged(MappedDataFile file);

}
