package org.dawnsci.datavis.model;

import java.util.Comparator;
import java.util.List;

import org.dawnsci.datavis.api.IFileOpeningController;
import org.eclipse.ui.progress.IProgressService;

/**
 * Interface to core class responsible for management of the state of a collection of files,
 * and the datasets therein.
 * <p>
 * The state refers to the collection of files and the contained datasets, whether the files
 * and datasets have been selected, and how the datasets are configured.
 * <p>
 * Interested objects can add listeners to be informed when the file state changes,
 * ideally they should read an immutable snapshot of that state if they want to 
 * update against the change.
 * <p>
 * Files are likely to be added and have state updated asynchronously, so methods which update
 * or the internal state are synchronised where necessary. 
 * <p>
 * Helper methods, which only call methods on the API and do not rely on the internal structure
 * should be added to the FileControllerUtils class as static methods, to keep the API simple.
 * <p>
 */
public interface IFileController extends IFileOpeningController {
	
	/**
	 * Load the files in the paths array into the file controller.
	 * <p>
	 * Loading occurs in a separate thread (but may block depending on the behaviour of the IProgressService.
	 * The IProgressService is used to provide feed back to the user, and can be null.
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
	List<String> loadFiles(String[] paths, IProgressService progressService, boolean addPlace);
	
	/**
	 * Unload files from the file controller
	 * 
	 * @param files
	 */
	public void unloadFiles(List<LoadedFile> files);

	/**
	 * Set the loaded file to be selected based on the supplied boolean
	 * 
	 * @param file
	 * @param selected
	 */
	void setFileSelected(LoadedFile file, boolean selected);

	/**
	 * Set the dataset to be selected based on the supplied boolean
	 * 
	 * @param data
	 * @param selected
	 */
	void setDataSelected(DataOptions data, boolean checked);
	
	/**
	 * Add a listener
	 * 
	 * @param l
	 */
	void addStateListener(FileControllerStateEventListener l);

	/**
	 * Remove a listener
	 * 
	 * @param l
	 */
	public void removeStateListener(FileControllerStateEventListener l);
	
	/**
	 * Returns an unchanging snapshot containing only the selected data options in selected files.
	 * 
	 * @return dataState
	 */
	List<DataOptions> getImmutableFileState();
	
	/**
	 * Used to rearrange the order of the files in the list
	 * <p>
	 * Won't appear to do anything if a {@link Comparator} is set in
	 * {@link #setComparator(Comparator<LoadedFile> comparator)}
	 * 
	 * @param files
	 * @param marker
	 */
	void moveBefore(List<LoadedFile> files, LoadedFile marker);
	
	/**
	 * Set a comparitor to sort the list of files returned
	 * by {@link #getLoadedFiles()}
	 * 
	 * @param comparator
	 */
	void setComparator(Comparator<LoadedFile> comparator);
	
	/**
	 * Set the name of the dataset to be used as the file label
	 * 
	 * @param label
	 */
	void setLabelName(String label);
	
	/**
	 * Checks for an implementation of {@link ILiveFileService} and attaches a
	 * listener for data updates
	 */
	public void attachLive();

	/**
	 * Removes listener from {@link ILiveFileService}
	 */
	public void detachLive();

	public List<LoadedFile> getLoadedFiles();

	public void deselect(List<IDataObject> objects);

	
	public void selectFiles(List<LoadedFile> files, boolean selected);

	public boolean isOnlySignals();

	public void setOnlySignals(boolean onlySignals);

	public void applyToAll(LoadedFile f);
	
	/**
	 * Supply a validator to check and correct the state within the file controller.
	 * <p>
	 * It might be desirable that some files and datasets might not be
	 * selectable at the same time, i.e. if two datasets are selected to be shown as images
	 * and the plot can't do this.
	 * 
	 * @param validator
	 */
	public void validateState(IFileStateValidator validator);
	
	/**
	 * DataOptions store objects describing their state, the file initialiser
	 * can be used to create these objects at the time of DataObjects construction
	 * 
	 * @param initialiser
	 */
	public void setFileInitialiser(ILoadedFileInitialiser initialiser);

}