package org.dawnsci.datavis.model;

public interface IRefreshable {

	public IRefreshable refresh();
	
	public void locallyReload();
	
	public boolean isLive();
	
	public boolean hasFinished();
	
	public boolean isEmpty();
	
}
