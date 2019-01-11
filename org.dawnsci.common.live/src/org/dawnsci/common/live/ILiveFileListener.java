package org.dawnsci.common.live;

import java.util.EventListener;

/**
 * Base interface for a live file listener.
 * <p>
 * Informs when the file should be refreshed to see the new shape of datasets,
 * and also when the file is closed and can be reloaded directly from disk.
 *
 */
public interface ILiveFileListener extends EventListener {

	public void refreshRequest();
	
	public void localReload(String path, boolean force);
	
}
