package org.dawnsci.datavis.model;

public interface IRefreshable {

	public void refresh();
	
	public void locallyReload();
	
	public boolean isLive();
	
	public boolean hasFinished();
	
}
