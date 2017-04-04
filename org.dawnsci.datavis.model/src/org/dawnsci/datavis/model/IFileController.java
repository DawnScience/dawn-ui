package org.dawnsci.datavis.model;

import java.util.List;

import org.eclipse.ui.progress.IProgressService;

public interface IFileController {

	public void loadFiles(String[] paths, IProgressService progressService);

	public void loadFile(String path);

	public void attachLive();
	
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