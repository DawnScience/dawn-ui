package org.dawnsci.datavis.model;

import java.util.EventListener;

public interface ILiveFileListener extends EventListener {

	public void fileLoaded(LoadedFile loadedFile);
	
	public void refreshRequest();
	
	public void localReload(String path);
	
}
