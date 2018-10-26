package org.dawnsci.mapping.ui;

import java.util.EventListener;

public interface ILiveMapFileListener extends EventListener {
	
	public void fileLoadRequest(String[] paths, String host, int port, String parent);
	
	public void refreshRequest();
	
	public void localReload(String path, boolean force);

}
