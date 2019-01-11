package org.dawnsci.mapping.ui;

import org.dawnsci.common.live.ILiveFileService;

/**
 * Extended {@link ILiveFileService} for the Mapping perspective.
 * <p>
 * Includes a method for automatically loading persisted files
 */
public interface ILiveMappingFileService  extends ILiveFileService{
	
	public void setInitialFiles(String[] files);
	
}
