package org.dawnsci.datavis.api;

import java.util.List;

public interface IFileOpeningController {
	
	/**
	 * Load the files in the paths array into the file controller.
	 * <p>
	 * The addPlace boolean indicates whether the location the file is loaded from should be
	 * set as the current recent location or not.
	 * <p>
	 * returns a list of paths that correspond to files which failed to load.
	 * 
	 * @param paths
	 * @param progressService
	 * @param addPlace
	 * @return failed
	 */
	List<String> loadFiles(String[] paths, boolean addPlace);

}
