package org.dawnsci.datavis.model;

import java.util.EventListener;

public interface FileControllerStateEventListener extends EventListener {
	
	void stateChanged(FileControllerStateEvent event);
	
	/**
	 * Called when the state hasn't changed but something that alters the display (size change, filter datasets) has
	 */
	default void refreshRequest() {
		//Do nothing as default
	}

}
