package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.api.IScriptFileOpener;
import org.dawnsci.datavis.model.fileconfig.CurrentStateFileConfiguration;
import org.dawnsci.datavis.model.fileconfig.ILoadedFileConfiguration;
import org.dawnsci.datavis.model.fileconfig.ImageFileConfiguration;
import org.dawnsci.datavis.model.fileconfig.NexusFileConfiguration;
import org.dawnsci.datavis.model.fileconfig.XYEFileConfiguration;
import org.dawnsci.january.model.NDimensions;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class FileController implements IFileController {

	private ILoaderService loaderService;
	private IRecentPlaces recentPlaces;
	private IScriptFileOpener scriptOpener;
	
	public void setLoaderService(ILoaderService service) {
		this.loaderService = service;
	}
	
	public void setRecentPlaces(IRecentPlaces places) {
		recentPlaces = places;
	}
	
	public void setScriptOpener(IScriptFileOpener scriptOpener) {
		this.scriptOpener = scriptOpener;
	}

	private LoadedFiles loadedFiles;
	private ILiveLoadedFileListener listener = new LiveFileListener();
	private AtomicBoolean onlySignals = new AtomicBoolean(false);
	private String labelName;
	
	private ILoadedFileConfiguration[] fileConfigs = new ILoadedFileConfiguration[]{new CurrentStateFileConfiguration(), new NexusFileConfiguration(), new ImageFileConfiguration(), new XYEFileConfiguration()};
	private Set<FileControllerStateEventListener> listeners;

	private ILoadedFileInitialiser fileInitialiser;
	
	private OpenMode openMode = OpenMode.DO_NOTHING;
	
	private static final Logger logger = LoggerFactory.getLogger(FileController.class);

	public FileController() {
		loadedFiles = new LoadedFiles();
		listeners = new HashSet<>();
		try {
			String m = System.getProperty("datavis.open.mode");
			if (m != null) {
				openMode = OpenMode.valueOf(m.toUpperCase());
			}
		} catch (IllegalArgumentException e) {
			logger.debug("Could not interpret open mode");
		}
	}

	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#loadFiles(java.lang.String[], org.eclipse.ui.progress.IProgressService, boolean)
	 */
	public List<String> loadFiles(String[] paths, IProgressService progressService, boolean addToRecentPlaces) {
		
		FileLoadingRunnable runnable = new FileLoadingRunnable(paths, addToRecentPlaces);
		
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
	
	public List<String> loadFiles(String[] paths, boolean addPlace) {
		return loadFiles(paths,(IProgressService)null,addPlace);
	}
	
	public void attachLive() {
		ServiceProvider.getOptionalService(ILiveLoadedFileService.class)
				.ifPresent(service -> service.addLiveFileListener(listener));
	}
	
	public void detachLive() {
		ServiceProvider.getOptionalService(ILiveLoadedFileService.class)
				.ifPresent(service -> service.removeLiveFileListener(listener));
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
		
		if (objects.isEmpty()) return;
		
		for (IDataObject o : objects) {
			if (o instanceof DataOptions) {
				((DataOptions)o).setSelected(false);
			} else if (o instanceof LoadedFile) {
				((LoadedFile)o).setSelected(false);
			}
		}

		fireStateChangeListeners(false, false, null, null);
	}

	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#selectFiles(java.util.List, boolean)
	 */
	@Override
	public void selectFiles(List<LoadedFile> files, boolean selected) {
		if (files.isEmpty())return;
		for (LoadedFile file : files) file.setSelected(selected);

		fireStateChangeListeners(true, true, files.get(0), null);
	}

	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#setCurrentFile(org.dawnsci.datavis.model.LoadedFile, boolean)
	 */
	@Override
	public void setFileSelected(LoadedFile file, boolean selected) {
		file.setSelected(selected);
		fireStateChangeListeners(true, true, file, getFirstSelectedOption(file));
	}

	private DataOptions getFirstSelectedOption(LoadedFile f) {
		DataOptions out = null;
		List<DataOptions> dd = f.getDataOptions();
		
		for (DataOptions d : dd) {
			if (d.isSelected())return d;
		}
		
		return out;
	}
	
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#setCurrentData(org.dawnsci.datavis.model.DataOptions, boolean)
	 */
	@Override
	public void setDataSelected(DataOptions data, boolean selected) {
		data.setSelected(selected);
		fireStateChangeListeners(false, true, data.getParent(), data);
	}

	@Override
	public void moveBefore(List<LoadedFile> files, LoadedFile marker) {
		loadedFiles.moveBefore(files, marker);
		fireStateChangeListeners(true, true,null,null);
	}

	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#unloadFiles(java.util.List)
	 */
	@Override
	public void unloadFiles(List<LoadedFile> files){
		
		if (files.isEmpty()) return;
		
		for (LoadedFile file : files) {
			loadedFiles.unloadFile(file);
		}

		fireStateChangeListeners(true, true, files.get(0), null);
	}

	private void fireUpdateRequest() {
		for (FileControllerStateEventListener l : listeners) l.refreshRequest();
	}
	
	private void fireStateChangeListeners(boolean file, boolean dataset, LoadedFile f, DataOptions o) {
		FileControllerStateEvent e = new FileControllerStateEvent(this, file, dataset, f , o);
		for (FileControllerStateEventListener l : listeners) l.stateChanged(e);
	}
	
	private void fireStateChangeListenersLoad(boolean file, boolean dataset, LoadedFile f, DataOptions o) {
		FileControllerStateEvent e = new FileControllerStateEvent(this, file, dataset, f , o);
		
		if (openMode != OpenMode.DO_NOTHING) {
			e.setPushSelectionUpdate(true);
		}
		
		for (FileControllerStateEventListener l : listeners) l.stateChanged(e);
	}

	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.IFileController#getImmutableFileState()
	 */
	@Override
	public List<DataOptions> getImmutableFileState() {
		
		List<DataOptions> list = new ArrayList<DataOptions>();
		
		for (LoadedFile f : getLoadedFiles()) {
			if (f.isSelected()) {
				String l = f.getLabel();
				for (DataOptions d : f.getDataOptions()) {
					if (d.isSelected()) {
						DataOptions dClone = d.clone();
						dClone.setLabel(l);
						list.add(dClone);
					}
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

		private final String[] paths;
		private final List<String> failedPaths;
		private final boolean addToRecentPlaces;
		
		public FileLoadingRunnable(String[] paths, boolean addToRecentPlaces) {
			this.paths = paths;
			this.addToRecentPlaces = addToRecentPlaces;
			failedPaths = new ArrayList<String>();
			
		}
		
		@Override
		public void run(IProgressMonitor monitor) {

			
			List<LoadedFile> files = new ArrayList<>();
			
			if (monitor != null) {
				monitor.beginTask("Loading files", paths.length);
			}
			
			for (String path : paths) {
				
				if (scriptOpener != null && scriptOpener.canOpen(path)) {
					scriptOpener.open(path);
					continue;
				}
				
				if (loadedFiles.contains(path)) continue;
				if (monitor != null) monitor.subTask("Loading " + path + "...");
				LoadedFile f = null;
				try {
					f = new LoadedFile(loaderService.getData(path,true, null));
				} catch (Exception e) {
					failedPaths.add(path);
					logger.error("Exception loading file",e);
				}
				
				if (f != null) {
					
					List<DataOptions> state = getImmutableFileState();
					
					for (ILoadedFileConfiguration c : fileConfigs) {
						try {
							c.setCurrentState(state);
							if (c.configure(f)) {
								break;
							}
						} catch (Exception e) {
							logger.error("Error thrown in {}", c, e);
						}
						
					}
					
					f.setLabelName(labelName);
					f.setOnlySignals(onlySignals.get());
					if (fileInitialiser != null) fileInitialiser.initialise(f);
					files.add(f);
				}
				
				if (monitor != null) monitor.worked(1);
				if (monitor != null && monitor.isCanceled()) {
					break;
				}
				
			}
			
			if (!files.isEmpty()) {
				if (addToRecentPlaces) {
					String[] filePaths = files.stream().map(LoadedFile::getFilePath).toArray(String[]::new);
					recentPlaces.addFiles(filePaths);
				}
				
				loadedFiles.addFiles(files);
				
				if (!openMode.equals(OpenMode.DO_NOTHING)) {
					LoadedFile loadedFile = files.get(files.size()-1);
					selectLoad(loadedFile, openMode.equals(OpenMode.DESELECT_OTHERS));
					return;
				}
			}

			fireStateChangeListenersLoad(false, false, files.isEmpty() ? null : files.get(0), null);
		}
		
		public List<String> getFailedLoadingFiles(){
			return failedPaths;
		}
	}

	private void selectLoad(LoadedFile f, boolean deselect) {
		f.setSelected(true);
		
		if (deselect) {
			loadedFiles.deselectOthers(f.getFilePath());
		}
		
		List<DataOptions> sd = f.getSelectedDataOptions();
		
		fireStateChangeListenersLoad(true, false, f, sd.isEmpty() ? null : sd.get(0));
	}
	
	@Override
	public void setComparator(Comparator<LoadedFile> comparator) {
		loadedFiles.setComparator(comparator);
		fireStateChangeListeners(true, true, null, null);
	}

	@Override
	public synchronized void setLabelName(String label) {
		labelName = label;
		for (LoadedFile file : loadedFiles) {
			file.setLabelName(label);
		}

		fireStateChangeListeners(false, true, null, null);
	}

	@Override
	public boolean isOnlySignals() {
		return onlySignals.get();
	}

	@Override
	public void setOnlySignals(boolean onlySignals) {
		this.onlySignals.set(onlySignals);
		loadedFiles.getLoadedFiles().stream().forEach(f-> f.setOnlySignals(onlySignals));

		fireStateChangeListeners(false, false, null, null);
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
						d2.setAxes(ndims.buildAxesNames());
					}
					
				}
			}
		}

		fireStateChangeListeners(false, true, null, null);
	}

	private class LiveFileListener implements ILiveLoadedFileListener {

		private AtomicReference<List<DataOptions>> cache = new AtomicReference<>();
		
		public LiveFileListener() {
		}

		/**
		 * Get the state of the file controller
		 * 
		 * If the file cannot be read on load a
		 * cache of the state is taken, in case
		 * it is changed (by automatic deselection).
		 * 
		 * @return
		 */
		private List<DataOptions> getFileState() {
			List<DataOptions> list = cache.getAndSet(null);
			
			if (list == null) {
				return getImmutableFileState();
			} else {
				return list;
			}
		}
		
		@Override
		public void refreshRequest() {

			Runnable r = new Runnable() {

				@Override
				public void run() {
					
					
					List<IRefreshable> fileList = loadedFiles.getLoadedFiles().stream()
							.filter(IRefreshable.class::isInstance)
							.map(IRefreshable.class::cast)
							.filter(IRefreshable::isLive)
							.collect(Collectors.toList());
					
					boolean toInit = false;
					
					for (IRefreshable r : fileList) {
						r.refresh();
						
						if (!r.isEmpty() && !r.isInitialised()) {
							toInit = true;
						}
					}
					
					if (toInit) {
						
						List<DataOptions> fs = getFileState();
						
						ILoadedFileConfiguration loadedConfig = null;
						if (!fs.isEmpty()) {
							loadedConfig = new CurrentStateFileConfiguration();
							loadedConfig.setCurrentState(fs);
						}

						ILoadedFileConfiguration nexusConfig = new NexusFileConfiguration();
						for (IRefreshable r : fileList) {
							if (!r.isEmpty() && !r.isInitialised()) {
								initialiseLiveFile((LoadedFile)r, loadedConfig, nexusConfig);
							}
						}
						if (!fileList.isEmpty()) {
							IRefreshable r = fileList.get(0);
							List<DataOptions> c = ((LoadedFile)r).getChecked();

							fireStateChangeListeners(true, true, (LoadedFile)fileList.get(0), !c.isEmpty() ? (DataOptions)c.get(0) : null);
						}
						
					}
					
					if (!fileList.isEmpty()) {
						fireUpdateRequest();
					}
				}
			};

			ServiceProvider.getService(ILiveLoadedFileService.class).runUpdate(r,false);
		}

		@Override
		public void localReload(String path, boolean force) {
			
			Runnable r = () -> {

				DataOptions dop = null;
				LoadedFile loadedFile = loadedFiles.getLoadedFile(path);
				if (loadedFile instanceof IRefreshable) {
					logger.info("Locally reloading {}",path);
					((IRefreshable)loadedFile).locallyReload();
					if (!((IRefreshable) loadedFile).isInitialised()) {
						List<DataOptions> fs = getFileState();
						
						try {

							ILoadedFileConfiguration loadedConfig = null;

							if (!fs.isEmpty()) {
								loadedConfig = new CurrentStateFileConfiguration();
								loadedConfig.setCurrentState(fs);
							}

							ILoadedFileConfiguration nexusConfig = new NexusFileConfiguration();

							initialiseLiveFile(loadedFile, loadedConfig, nexusConfig);

							List<DataOptions> c = loadedFile.getChecked();
							dop = c.isEmpty() ? null : c.get(0);


						} catch (Exception e) {
							logger.warn("Could not configure file",e);
						}
					}
				}
				if (Display.getCurrent() == null) {
					final DataOptions d = dop;
					//This should be else where but it breaks the unit tests due to locking in Display (only for true,true)
					Display.getDefault().asyncExec(() -> fireStateChangeListeners(true, true, loadedFile, d));
				} else {
					fireStateChangeListeners(true, true, loadedFile, dop);
				}
			};
			
			ServiceProvider.getService(ILiveLoadedFileService.class).runUpdate(r,true);
		}

		@Override
		public void fileLoaded(LoadedFile loadedFile) {
			
			loadedFile.setOnlySignals(onlySignals.get());
			loadedFile.setLabelName(labelName);

			if (loadedFile instanceof IRefreshable && !((IRefreshable)loadedFile).isEmpty()) {
				List<DataOptions> fs = getImmutableFileState();
				try {

					ILoadedFileConfiguration loadedConfig = null;

					if (!fs.isEmpty()) {
						loadedConfig = new CurrentStateFileConfiguration();
						loadedConfig.setCurrentState(fs);
					}

					ILoadedFileConfiguration nexusConfig = new NexusFileConfiguration();
					
					initialiseLiveFile(loadedFile, loadedConfig, nexusConfig);
					
				} catch (Exception e) {
						logger.warn("Could not configure file",e);
				}

			} else if (loadedFile instanceof IRefreshable && ((IRefreshable)loadedFile).isEmpty()) {
				cache.set(getImmutableFileState());
			}
			
			loadedFiles.addFile(loadedFile);

			if (!openMode.equals(OpenMode.DO_NOTHING)) {
				selectLoad(loadedFile, openMode.equals(OpenMode.DESELECT_OTHERS));
				return;
			}
			
			fireStateChangeListenersLoad(false, false, loadedFile, null);
		}
		
		private void initialiseLiveFile(LoadedFile file, ILoadedFileConfiguration primary, ILoadedFileConfiguration secondary) {
			if (primary == null || !primary.configure(file)) {
				secondary.configure(file);
			}
			
			if (fileInitialiser != null) {
				fileInitialiser.initialise(file);
				((IRefreshable)file).setInitialised();
			}
		}
	}
	

	
	public void validateState(IFileStateValidator validator) {
		
		List<LoadedFile> lf = loadedFiles.getLoadedFiles();
		
		LoadedFile changed = null;
		
		for (LoadedFile f : lf) {
			boolean valid= validator.validate(f);
			if (changed == null && !valid) {
				changed = f;
			}
		}
		
		fireStateChangeListeners(false, false, null, null);
		
	}
	
	public void setFileInitialiser(ILoadedFileInitialiser initialiser) {
		fileInitialiser = initialiser;
	}

	@Override
	public OpenMode getOpenMode() {
		return openMode;
	}

	@Override
	public void setOpenMode(OpenMode openMode) {
		this.openMode = openMode;
		
	}
}
