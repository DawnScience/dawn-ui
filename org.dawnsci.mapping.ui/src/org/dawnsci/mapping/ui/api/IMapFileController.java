package org.dawnsci.mapping.ui.api;

import java.util.List;

import org.dawnsci.mapping.ui.IRegistrationHelper;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.IMapFileEventListener;
import org.dawnsci.mapping.ui.datamodel.LiveDataBean;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.ui.progress.IProgressService;


/**
 * Interface to the file controller in the Mapping perspective.
 * 
 * Use to gain information about the current state of the file
 * loaded, and perform actions on those files
 * 
 */
public interface IMapFileController {

	/**
	 * Unplots all data from the files in the supplied list
	 * 
	 * @param file
	 */
	void removeFromDisplay(List<MappedDataFile> file);
	
	/**
	 * Force all plotted data to be cleared
	 */
	void removeAllFromDisplay();

	/**
	 * Remove event listener
	 * 
	 * @param l
	 */
	void removeListener(IMapFileEventListener l);

	/**
	 * Add event listener
	 * 
	 * @param l
	 */
	void addListener(IMapFileEventListener l);
	
	/**
	 * Add a (usually visible) image that can be
	 * plotted with the map data
	 * 
	 * @param image
	 */
	void addAssociatedImage(AssociatedImage image);

	/**
	 * Get the mapped data area
	 * 
	 * @return
	 */
	MappedDataArea getArea();

	/**
	 * Get all plotted objects
	 * 
	 * @return
	 */
	List<PlottableMapObject> getPlottedObjects();

	/**
	 * Check if controller contains a file
	 * 
	 * @param path
	 * @return
	 */
	boolean contains(String path);

	/**
	 * Remove all loaded files from the controller
	 */
	void clearAll();

	/**
	 * Determine if there are any live scan files
	 * 
	 * @return
	 */
	boolean containsLiveFiles();

	/**
	 * Remove all files that are not currently live
	 * 
	 */
	void clearNonLiveFiles();

	/**
	 * Remove a file associated with the supplied path
	 * 
	 * @param path
	 */
	void removeFile(String path);

	/**
	 * Replace a live file with direct read from disk
	 * 
	 * @param path
	 * @param force
	 */
	void localReloadFile(String path, boolean force);

	/**
	 * Load a live files using remote loading
	 * 
	 * @param path
	 * @param bean
	 * @param parentFile
	 * @param lazy
	 */
	void loadLiveFile(final String path, LiveDataBean bean, String parentFile, boolean lazy);

	/**
	 * Load a file from a bean
	 * 
	 * @param path
	 * @param bean
	 * @param progressService
	 * @return
	 */
	List<String> loadFile(String path, MappedDataFileBean bean, IProgressService progressService);

	/**
	 * Load files
	 * 
	 * @param paths
	 * @param progressService
	 * @return
	 */
	List<String> loadFiles(String[] paths, IProgressService progressService);
	
	/**
	 * Load these paths as live files
	 * 
	 * @param paths
	 */
	void attachLive(String[] paths);

	/**
	 * Toggle whether the object is plotted then update listeners
	 * 
	 * @param object
	 */
	void toggleDisplay(PlottableMapObject object);

	/**
	 * Set helper to register images
	 * 
	 * @param helper
	 */
	void setRegistrationHelper(IRegistrationHelper helper);

	/**
	 * Unload file from controller
	 * 
	 * @param file
	 */
	void removeFile(MappedDataFile file);
	
	/**
	 * Add a live stream object
	 * 
	 * @param stream
	 */
	void addLiveStream(LiveStreamMapObject stream);
	
	/**
	 * Update controller due to changed file state
	 * 
	 * @param file
	 */
	void registerUpdates(MappedDataFile file);
}

