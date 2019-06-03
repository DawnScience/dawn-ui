package org.dawnsci.datavis.model;

import org.dawnsci.datavis.api.ILiveFileListener;

/**
 * Extended {@link ILiveFileListener} for the DataVis perspective.
 * <p>
 * Allows subscribers to be notified when a live file is loaded
 */
public interface ILiveLoadedFileListener extends ILiveFileListener {

	public void fileLoaded(LoadedFile loadedFile);
	
}
