package org.dawnsci.datavis.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.progress.IProgressService;

public class FileController {

	private static final FileController instance = new FileController();
	
	private LoadedFiles loadedFiles;
	private LoadedFile currentFile;
	private DataOptions currentData;
	
	private Set<FileControllerStateEventListener> listeners = new HashSet<FileControllerStateEventListener>();
	
	private LinkedList<String> lastFile = new LinkedList<String>(); 
	
	private FileController(){
		loadedFiles = new LoadedFiles();
	};
	
	public static FileController getInstance() {
		return instance;
	}
	
	public void loadFiles(String[] paths, IProgressService progressService) {
		
		FileLoadingRunnable runnable = new FileLoadingRunnable(paths);
		
		if (progressService == null) {
			runnable.run(null);
		} else {
			try {
				progressService.busyCursorWhile(runnable);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
//		IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
		
			
	}
	
	
	public void loadFile(String path) {
		loadFiles(new String[]{path}, null);
	}
	
	public LoadedFiles getLoadedFiles() {
		return loadedFiles;
	}
	
	public void deselectFile(LoadedFile file) {
		file.setSelected(false);
		fireStateChangeListeners(false,false);
	}
	
	public void deselectOption(DataOptions option) {
		option.setSelected(false);
		fireStateChangeListeners(false,false);
	}
	
	public void deselectAllOthers() {
		List<DataOptions> dataOptions = currentFile.getDataOptions();
		for (DataOptions dop : dataOptions) {
			if (currentData != dop) dop.setSelected(false);
		}
		loadedFiles.deselectOthers(currentFile.getLongName());
		
		fireStateChangeListeners(false,false);
	}
	
	public void selectFiles(List<LoadedFile> files, boolean selected) {
		for (LoadedFile file : files) file.setSelected(selected);
		fireStateChangeListeners(true,true);
		
	}
	
	public void setCurrentFile(LoadedFile file, boolean selected) {
		if (file == currentFile && selected == currentFile.isSelected()) return;
		currentFile = file;
		if (currentFile == null) {
			currentData = null;
			return;
		}
		
		
		file.setSelected(selected);
		
		DataOptions option = null;
		
		for (DataOptions op : file.getDataOptions()) {
			if (op.isSelected()) {
				option = op;
				break;
			}
		}
		
		if (option == null && file.getDataOptions().size() != 0) {
			option = file.getDataOptions().get(0);
		}
		
		if (option == null) return;
		
		setCurrentDataOnFileChange(option);
		
	}
	
	public void setCurrentDataOnFileChange(DataOptions data) {
		currentData = data;
		fireStateChangeListeners(true,true);
	}
	
	public void setCurrentData(DataOptions data, boolean selected) {
		if (currentData == data && data.isSelected() == selected) return;
		currentData = data;
		data.setSelected(selected);
		fireStateChangeListeners(false,true);
	}
	
	public void setCurrentData(DataOptions data) {
		if (currentData == data) return;
		currentData = data;
		fireStateChangeListeners(false,true);
	}
	
	public DataOptions getCurrentDataOption() {
		return currentData;
	}
	
	public void unloadFile(LoadedFile file){
		loadedFiles.unloadFile(file);
		if (currentFile == file)  {
			currentFile = null;
			currentData = null;
		}
		fireStateChangeListeners(true, true);
	}
	
	public void unloadFiles(List<LoadedFile> files){
		
		for (LoadedFile file : files){

			loadedFiles.unloadFile(file);
		if (currentFile == file)  {
			currentFile = null;
			currentData = null;
		}
	}
		fireStateChangeListeners(true, true);
	}
	
	public LoadedFile getCurrentFile() {
		return currentFile;
	}
	
	public List<DataOptions> getSelectedDataOptions(){
		
		List<DataOptions> checked = new ArrayList<>();
		
		for (DataOptions op : currentFile.getDataOptions()) {
			if (op.isSelected()) checked.add(op);
		}
		return checked;
	}
	
	public List<LoadedFile> getSelectedFiles(){
		
		List<LoadedFile> checked = new ArrayList<>();
		
		for (LoadedFile f : loadedFiles) {
			if (f.isSelected()) checked.add(f);
		}
		return checked;
	}
	
	public int getSelectedDataRank() {
		if (currentData == null) return -1;
		int[] shape = currentData.getData().getShape();
		shape = ShapeUtils.squeezeShape(shape, false);
		int rank = shape.length;
		return rank;
	}

	private void fireStateChangeListeners(boolean file, boolean dataset) {
		FileControllerStateEvent e = new FileControllerStateEvent(this, file, dataset);
		for (FileControllerStateEventListener l : listeners) l.stateChanged(e);
	}
	
	public void addStateListener(FileControllerStateEventListener l) {
		listeners.add(l);
	}
	
	public void removeStateListener(FileControllerStateEventListener l) {
		listeners.remove(l);
	}
	
	private class FileLoadingRunnable implements IRunnableWithProgress {

		String[] paths;
		
		public FileLoadingRunnable(String[] paths) {
			this.paths = paths;
		}
		
		@Override
		public void run(IProgressMonitor monitor) {
			
			List<LoadedFile> files = new ArrayList<>();
			
			for (String path : paths) {
				//test path isnt already contained
				LoadedFile f = null;
				try {
					f = new LoadedFile(ServiceManager.getLoaderService().getData(path, null));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (f != null) files.add(f);
				
			}
			
			if (!files.isEmpty()) {
				String name = files.get(0).getLongName();
				File f = new File(name);
				File parentFile = f.getParentFile();
				if (parentFile != null) {
					String parentPath = parentFile.getAbsolutePath();
					if (lastFile.size() > 5) lastFile.removeLast();
					if (!lastFile.contains(parentPath)){
						lastFile.addFirst(parentPath);
					} else {
						lastFile.remove(parentPath);
						lastFile.addFirst(parentPath);
					}
				}
				loadedFiles.addFiles(files);
			}
			

//			
			
			fireStateChangeListeners(false,false);
			
		}
		
	}

	public void unloadAll() {
		loadedFiles.unloadAllFiles();

		currentFile = null;
		currentData = null;
		
		fireStateChangeListeners(true, true);
	}
	
	public void applyToAll(LoadedFile f) {
		
		List<DataOptions> selected = f.getSelectedDataOptions();
		
		for (LoadedFile file : loadedFiles) {
			
			if (file == f) continue;
			
			for (DataOptions d : selected) {
				DataOptions d2 = file.getDataOption(d.getName());
				if (d2 != null && Arrays.equals(d2.getData().getShape(), d.getData().getShape())) {
					d2.setSelected(true);
					
					if (d.getPlottableObject() != null) {
						NDimensions oDims = d.getPlottableObject().getNDimensions();
						NDimensions ndims = new NDimensions(oDims);
						
						PlottableObject plotOb = new PlottableObject(d.getPlottableObject().getPlotMode(), ndims);
						d2.setPlottableObject(plotOb);
					}
					
				}
			}
		}
		
		fireStateChangeListeners(false, true);
		
	}
	
	public Collection<String> getLastFolders(){
		return lastFile;
	}
}
