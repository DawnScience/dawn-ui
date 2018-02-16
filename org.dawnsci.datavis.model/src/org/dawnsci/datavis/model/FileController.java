package org.dawnsci.datavis.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.fileconfig.CurrentStateFileConfiguration;
import org.dawnsci.datavis.model.fileconfig.ILoadedFileConfiguration;
import org.dawnsci.datavis.model.fileconfig.ImageFileConfiguration;
import org.dawnsci.datavis.model.fileconfig.NexusFileConfiguration;
import org.dawnsci.datavis.model.fileconfig.XYEFileConfiguration;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileController implements IFileController {
	
	private ILoaderService loaderService;
	private IRecentPlaces recentPlaces;
	
	public void setLoaderService(ILoaderService service) {
		this.loaderService = service;
	}
	
	public void setRecentPlaces(IRecentPlaces places) {
		recentPlaces = places;
	}
	
	private Map<String, LoadedFiles> store;
	private LoadedFiles loadedFiles;
	private LoadedFile currentFile;
	private DataOptions currentData;
	private ILiveFileListener listener;
	
	private boolean onlySignals = false;

	private String labelName;
	
	private ILoadedFileConfiguration[] fileConfigs = new ILoadedFileConfiguration[]{new CurrentStateFileConfiguration(), new NexusFileConfiguration(), new ImageFileConfiguration(), new XYEFileConfiguration()};
	
	private Map<String, Set<FileControllerStateEventListener>> allListeners;
	private Set<FileControllerStateEventListener> listeners = new HashSet<>();
	
	private static final Logger logger = LoggerFactory.getLogger(FileController.class);
	
	public FileController(){
		store = new HashMap<>();
		allListeners = new HashMap<>();
	};

	@Override
	public synchronized void setID(String id) {
		if (!store.containsKey(id)) {
			store.put(id, new LoadedFiles());
		}
		loadedFiles = store.get(id);
		if (allListeners.isEmpty()) { // reuse default as first ID to avoid an NPE
			allListeners.put(id, listeners);
		} else if (!allListeners.containsKey(id)) {
			allListeners.put(id, new HashSet<>());
		}
		listeners = allListeners.get(id);
	}

	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#loadFiles(java.lang.String[], org.eclipse.ui.progress.IProgressService)
	 */
	@Override
	public List<String> loadFiles(String[] paths, IProgressService progressService) {
		
		FileLoadingRunnable runnable = new FileLoadingRunnable(loadedFiles, listeners, paths);
		
		if (progressService == null) {
			runnable.run(null);
		} else {
			try {
				progressService.busyCursorWhile(runnable);
			} catch (Exception e) {
				logger.debug("Busy while interrupted", e);
			} 
		}
		
		List<String> failed = runnable.getFailedLoadingFiles();
		return failed;
	}
	
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#loadFile(java.lang.String)
	 */
	@Override
	public void loadFile(String path) {
		loadFiles(new String[]{path}, null);
	}
	
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#joinFiles(java.lang.String)
	 */
	@Override
	public void joinFiles(List<LoadedFile> files) {
		// First, let's set up some variables that we'll be adding to
		LoadedFile firstFile = files.get(0);
		String neXusPath = "";
		String datasetsString = "";
		String fileNamesString = "# FILE_NAME";
		String directoryPath = "# DIR_NAME: " + firstFile.getParent() + "\n";
		
		// Then let's get some information about the NeXus tree
		Tree tree = firstFile.getTree();
		GroupNode groupNode = tree.getGroupNode();
		
		// Check that the file isn't empty...
		if (groupNode != null) {
			// And find all the datanodes within the file
			datasetsString = this.nodeSearcher(groupNode, neXusPath, datasetsString);
		}
		
		// Then generate the string for all the files within the passed list
		for (Iterator<LoadedFile> loopIter = files.iterator(); loopIter.hasNext(); ) {
			LoadedFile file = loopIter.next();
			fileNamesString += "\n" + file.getName();
		}
		
		// Now create a fileWriter
		BufferedWriter fileWriter = null;
		// Then come up with the output filename
		String firstFileName = files.get(0).getName();
		String lastFileName = files.get(files.size()-1).getName();
		
		int extensionLocation = firstFileName.lastIndexOf(".");
		String outputName = firstFileName.substring(0, extensionLocation) + "--" + lastFileName.substring(0, extensionLocation) + " ";
		File tempFile = null;
		
		// Now output the text into the .dawn file
		try {
			tempFile = File.createTempFile(outputName, ".dawn");
			tempFile.setWritable(true);
			tempFile.deleteOnExit();
			
			fileWriter = new BufferedWriter(new FileWriter(tempFile.getAbsoluteFile()));
			fileWriter.write(directoryPath + datasetsString + fileNamesString);
			fileWriter.close();
		} catch (IOException fileError) {
			// Print out if we have any errors
			logger.error("There was a problem creating the .dawn linker file: " + fileError.toString());
		}
		
		// And open the resulting file
		if (tempFile != null) {
			this.loadFile(tempFile.getAbsolutePath());
		}
	}
	
	
	private String nodeSearcher(GroupNode groupNode, String neXusPath, String datasetsString) {
		// Find out what we've been passed
		Collection<String> subNodeNames = groupNode.getNames();
		
		// And loop through it
		for (Iterator<String> subNodeNameIterator = subNodeNames.iterator(); subNodeNameIterator.hasNext(); ) {
			// Creating an iterator and going through all the nodes 
			String subNodeName = subNodeNameIterator.next();
			Node subNode = groupNode.getNode(subNodeName);
			// Building this as needed for our output file
			String currentPath = neXusPath + "/" + subNodeName;
		
			// Recursing if we've been passed a group node
			if (subNode.isGroupNode()) {
				datasetsString = this.nodeSearcher((GroupNode) subNode, currentPath, datasetsString);
			}
			
			// Logging it if we've been passed a data node
			if (subNode.isDataNode()) {
				datasetsString += "# DATASET_NAME: " + currentPath + "\n";
			}
		}
		// And returning the results of our investigation
		return datasetsString;
	}
	
	
	public void attachLive() {
		if (LiveServiceManager.getILiveFileService() != null) {
			
			if (listener == null) {
				listener = new LiveFileListener(loadedFiles, listeners);
			}
			
			LiveServiceManager.getILiveFileService().addLiveFileListener(listener);

		}
	}
	
	public void detachLive() {
		if (LiveServiceManager.getILiveFileService() != null && listener != null) {
			LiveServiceManager.getILiveFileService().removeLiveFileListener(listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#getLoadedFiles()
	 */
	@Override
	public List<LoadedFile> getLoadedFiles() {
		return loadedFiles.getLoadedFiles();
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#deselect(java.util.List)
	 */
	@Override
	public void deselect(List<IDataObject> objects) {
		
		for (IDataObject o : objects) {
			if (o instanceof DataOptions) {
				((DataOptions)o).setSelected(false);
			} else if (o instanceof LoadedFile) {
				((LoadedFile)o).setSelected(false);
			}
		}
		
		fireStateChangeListeners(false,false);
	}
	
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#selectFiles(java.util.List, boolean)
	 */
	@Override
	public void selectFiles(List<LoadedFile> files, boolean selected) {
		for (LoadedFile file : files) file.setSelected(selected);
		fireStateChangeListeners(true,true);
		
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#setCurrentFile(org.dawnsci.datavis.model.LoadedFile, boolean)
	 */
	@Override
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
		
		setCurrentDataOnFileChange(option);
		
	}
	
	private void setCurrentDataOnFileChange(DataOptions data) {
		currentData = data;
		fireStateChangeListeners(true,true);
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#setCurrentData(org.dawnsci.datavis.model.DataOptions, boolean)
	 */
	@Override
	public void setCurrentData(DataOptions data, boolean selected) {
		if (currentData == data && data.isSelected() == selected) return;
		currentData = data;
		data.setSelected(selected);
		fireStateChangeListeners(false,true);
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#getCurrentDataOption()
	 */
	@Override
	public DataOptions getCurrentDataOption() {
		return currentData;
	}
	
	public void moveBefore(List<LoadedFile> files, LoadedFile marker) {
		loadedFiles.moveBefore(files, marker);
		fireStateChangeListeners(true, true);
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#unloadFile(org.dawnsci.datavis.model.LoadedFile)
	 */
	@Override
	public void unloadFile(LoadedFile file){
		loadedFiles.unloadFile(file);
		if (currentFile == file)  {
			currentFile = null;
			currentData = null;
		}
		fireStateChangeListeners(true, true);
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#unloadFiles(java.util.List)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#getCurrentFile()
	 */
	@Override
	public LoadedFile getCurrentFile() {
		return currentFile;
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#getSelectedFiles()
	 */
	@Override
	public List<LoadedFile> getSelectedFiles(){
		
		List<LoadedFile> checked = new ArrayList<>();
		
		for (LoadedFile f : loadedFiles) {
			if (f.isSelected()) checked.add(f);
		}
		return checked;
	}
	
	private void fireStateChangeListeners(boolean file, boolean dataset) {
		fireStateChangeListeners(listeners, file, dataset);
	}
	
	private void fireStateChangeListeners(Set<FileControllerStateEventListener> listeners, boolean file, boolean dataset) {
		FileControllerStateEvent e = new FileControllerStateEvent(this, file, dataset);
		for (FileControllerStateEventListener l : listeners) l.stateChanged(e);
	}

	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#getImmutableFileState()
	 */
	@Override
	public List<DataStateObject> getImmutableFileState() {
		
		List<DataStateObject> list = new ArrayList<DataStateObject>();
		
		for (LoadedFile f : getLoadedFiles()) {
			for (DataOptions d : f.getDataOptions()) {
				
				PlottableObject plotObject = null; 
				
				if (d.getPlottableObject() != null) {
					PlottableObject p = d.getPlottableObject();
					plotObject = new PlottableObject(p.getPlotMode(), new NDimensions(p.getNDimensions()));
				}
				
				if (f.isSelected() && d.isSelected()) {
					DataStateObject dso = new DataStateObject(d, f.isSelected() && d.isSelected(), plotObject);
					
					list.add(dso);
				}
				
			}
		}
		
		return list;
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#addStateListener(org.dawnsci.datavis.model.FileControllerStateEventListener)
	 */
	@Override
	public void addStateListener(FileControllerStateEventListener l) {
		listeners.add(l);
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#removeStateListener(org.dawnsci.datavis.model.FileControllerStateEventListener)
	 */
	@Override
	public void removeStateListener(FileControllerStateEventListener l) {
		listeners.remove(l);
	}
	
	private class FileLoadingRunnable implements IRunnableWithProgress {

		private final LoadedFiles lFiles;
		private final Set<FileControllerStateEventListener> fListeners;
		private final String[] paths;
		private final List<String> failedPaths;
		
		public FileLoadingRunnable(LoadedFiles files, Set<FileControllerStateEventListener> listeners, String[] paths) {
			this.lFiles = files;
			this.fListeners = listeners;
			this.paths = paths;
			failedPaths = new ArrayList<String>();
		}
		
		@Override
		public void run(IProgressMonitor monitor) {
			
			List<LoadedFile> files = new ArrayList<>();
			
			if (monitor != null) {
				monitor.beginTask("Loading files", paths.length);
			}
			
			for (String path : paths) {
				if (lFiles.contains(path)) continue;
				if (monitor != null) monitor.subTask("Loading " + path + "...");
				LoadedFile f = null;
				try {
					f = new LoadedFile(loaderService.getData(path, null));
				} catch (Exception e) {
					failedPaths.add(path);
					logger.error("Exception loading file",e);
				}
				
				if (f != null) {
					
					List<DataStateObject> state = getImmutableFileState();
					
					for (ILoadedFileConfiguration c : fileConfigs) {
						c.setCurrentState(state);
						if (c.configure(f)) {
							break;
						}
					}
					
					f.setLabelName(labelName);
					f.setOnlySignals(onlySignals);
					files.add(f);
				}
				
				if (monitor != null) monitor.worked(1);
				if (monitor != null && monitor.isCanceled()) {
					break;
				}
				
			}
			
			if (!files.isEmpty()) {
				String name = files.get(0).getFilePath();
				recentPlaces.addPlace(name);
				lFiles.addFiles(files);
			}
			
			fireStateChangeListeners(fListeners, false,false);
			
		}
		
		public List<String> getFailedLoadingFiles(){
			return failedPaths;
		}
	}

	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#unloadAll()
	 */
	@Override
	public void unloadAll() {
		loadedFiles.unloadAllFiles();

		currentFile = null;
		currentData = null;
		
		fireStateChangeListeners(true, true);
	}
	
	public void setComparator(Comparator<LoadedFile> comparator) {
		loadedFiles.setComparator(comparator);
		fireStateChangeListeners(true, true);
	}
	
	public void setLabelName(String label) {
		labelName = label;
		for (LoadedFile file : loadedFiles) {
			file.setLabelName(label);
		}
		fireStateChangeListeners(false, true);
	}
	
	public boolean isOnlySignals() {
		return onlySignals;
	}

	public void setOnlySignals(boolean onlySignals) {
		this.onlySignals = onlySignals;
		loadedFiles.getLoadedFiles().stream().forEach(f-> f.setOnlySignals(onlySignals));
		
		fireStateChangeListeners(true, true);
	}
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#applyToAll(org.dawnsci.datavis.model.LoadedFile)
	 */
	@Override
	public void applyToAll(LoadedFile f) {
		
		List<DataOptions> selected = f.getSelectedDataOptions();
		
		for (LoadedFile file : loadedFiles) {
			
			if (file == f) continue;
			
			for (DataOptions d : selected) {
				DataOptions d2 = file.getDataOption(d.getName());
				if (d2 != null && Arrays.equals(d2.getLazyDataset().getShape(), d.getLazyDataset().getShape())) {
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
	
	private class LiveFileListener implements ILiveFileListener {
		private final LoadedFiles lFiles;
		private final Set<FileControllerStateEventListener> fListeners;

		public LiveFileListener(LoadedFiles files, Set<FileControllerStateEventListener> listeners) {
			this.lFiles = files;
			this.fListeners = listeners;
		}

		@Override
		public void refreshRequest() {

			Runnable r = new Runnable() {

				@Override
				public void run() {

					List<DataStateObject> fs = getImmutableFileState();

					ILoadedFileConfiguration loadedConfig = null;

					if (!fs.isEmpty()) {
						loadedConfig = new CurrentStateFileConfiguration();
						loadedConfig.setCurrentState(fs);
					}

					ILoadedFileConfiguration nexusConfig = new NexusFileConfiguration();

					List<IRefreshable> files = lFiles.getLoadedFiles().stream()
							.filter(IRefreshable.class::isInstance)
							.map(IRefreshable.class::cast).collect(Collectors.toList());

					for (IRefreshable file : files) {

						if (file.isEmpty()) {

							file.refresh();

							if (!file.isEmpty()) {
								if (loadedConfig != null && !loadedConfig.configure((LoadedFile)file)) {
									nexusConfig.configure((LoadedFile)file);
								}
							}

						} else {
							file.refresh();
						}
					}


					Display.getDefault().syncExec( () -> fireStateChangeListeners(fListeners, true, true));

				}
			};

			LiveServiceManager.getILiveFileService().runUpdate(r);
		}

		@Override
		public void localReload(String path) {
			LoadedFile loadedFile = lFiles.getLoadedFile(path);
			if (loadedFile instanceof IRefreshable) {
				((IRefreshable)loadedFile).locallyReload();
			}

		}

		@Override
		public void fileLoaded(LoadedFile loadedFile) {
			
			List<DataStateObject> fs = getImmutableFileState();
			
			loadedFile.setOnlySignals(onlySignals);

			if (loadedFile instanceof IRefreshable && !((IRefreshable)loadedFile).isEmpty()) {

				
				ILoadedFileConfiguration loadedConfig = null;

				if (!fs.isEmpty()) {
					loadedConfig = new CurrentStateFileConfiguration();
					loadedConfig.setCurrentState(fs);
				}

				ILoadedFileConfiguration nexusConfig = new NexusFileConfiguration();

				
				if (loadedConfig == null || !loadedConfig.configure(loadedFile)) {
					nexusConfig.configure((LoadedFile)loadedFile);
				}

			}
			
			loadedFile.setLabelName(labelName);
			
			lFiles.addFile(loadedFile);
			fireStateChangeListeners(fListeners, false, false);
		}

	}
}
