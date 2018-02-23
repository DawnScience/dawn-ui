package org.dawnsci.datavis.model;

import java.util.Comparator;
import java.util.List;

import org.eclipse.ui.progress.IProgressService;

public interface IFileController {

	public void setID(String id);

	public String getID();

	public void setComparator(Comparator<LoadedFile> comparator);
	
	public void setLabelName(String label);

	public boolean isOnlySignals();

	public void setOnlySignals(boolean onlySignals);

	public void moveBefore(List<LoadedFile> files, LoadedFile marker);

	public List<String> loadFiles(String[] paths, IProgressService progressService);

	/** load file path
	 * 
	 * @param path
	 * @return true if file was loaded successfully, false otherwise
	 */
	public boolean loadFile(String path);
	
	public void attachLive();

	public void detachLive();

	public List<LoadedFile> getLoadedFiles();

	public void deselect(List<IDataObject> objects);

	public void selectFiles(List<LoadedFile> files, boolean selected);

	public void setCurrentFile(LoadedFile file, boolean selected);

	public void setCurrentData(DataOptions data, boolean selected);

	public DataOptions getCurrentDataOption();

	public void unloadFile(LoadedFile file);

	public void unloadFiles(List<LoadedFile> files);

	public LoadedFile getCurrentFile();

	public List<LoadedFile> getSelectedFiles();

	public List<DataStateObject> getImmutableFileState();

	public void addStateListener(FileControllerStateEventListener l);

	public void removeStateListener(FileControllerStateEventListener l);

	public void unloadAll();

	public void applyToAll(LoadedFile f);

}