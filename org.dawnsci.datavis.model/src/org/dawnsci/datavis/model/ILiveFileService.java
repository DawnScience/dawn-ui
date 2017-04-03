package org.dawnsci.datavis.model;

public interface ILiveFileService {

	public void addLiveFileListener(ILiveFileListener l);
	
	public void removeLiveFileListener(ILiveFileListener l);
	
	public void attach();

	public void runUpdate(Runnable runnable);
}
