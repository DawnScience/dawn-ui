package org.dawnsci.common.live;

/**
 * Interface describing a service to deal with live files,
 * and resource for updating them.
 */
public interface ILiveFileService {

	public void addLiveFileListener(ILiveFileListener l);
	
	public void removeLiveFileListener(ILiveFileListener l);

	public void runUpdate(Runnable runnable, boolean queue);
	
}
