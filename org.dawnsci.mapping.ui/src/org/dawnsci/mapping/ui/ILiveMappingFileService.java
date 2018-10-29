package org.dawnsci.mapping.ui;


public interface ILiveMappingFileService {
	
	public void setInitialFiles(String[] files);
	
	public void addLiveFileListener(ILiveMapFileListener l);
	
	public void removeLiveFileListener(ILiveMapFileListener l);
	
	public void runUpdate(Runnable runnable, boolean queue);

}
