package org.dawnsci.datavis.model;

import java.util.EventListener;

public interface FileControllerStateEventListener extends EventListener {
	
	public void stateChanged(FileControllerStateEvent event);
	
	default public void liveUpdate() {
		//Do nothing as default
	}

}
