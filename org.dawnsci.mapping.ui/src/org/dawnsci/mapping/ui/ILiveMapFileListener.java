package org.dawnsci.mapping.ui;

import org.dawnsci.datavis.api.ILiveFileListener;

/**
 * Extended {@link ILiveFileListener} for the Mapping perspective.
 * <p>
 * Allows subscribers to be notified with a request to load a live file
 */
public interface ILiveMapFileListener extends ILiveFileListener {
	
	public void fileLoadRequest(String[] paths, String host, int port, String parent);

}
