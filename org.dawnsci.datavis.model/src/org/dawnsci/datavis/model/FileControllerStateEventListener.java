package org.dawnsci.datavis.model;

import java.util.EventListener;

public interface FileControllerStateEventListener extends EventListener {
	
	public void stateChanged(FileControllerStateEvent event);

}
