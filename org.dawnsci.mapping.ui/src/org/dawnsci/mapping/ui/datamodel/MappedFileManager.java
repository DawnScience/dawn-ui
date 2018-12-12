package org.dawnsci.mapping.ui.datamodel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.util.DatasetNameUtils;
import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.mapping.ui.BeanBuilderWizard;
import org.dawnsci.mapping.ui.IBeanBuilderHelper;
import org.dawnsci.mapping.ui.ILiveMapFileListener;
import org.dawnsci.mapping.ui.ILiveMappingFileService;
import org.dawnsci.mapping.ui.IRegistrationHelper;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.wizards.LegacyMapBeanBuilder;
import org.dawnsci.mapping.ui.wizards.MapBeanBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.tree.TreeToMapUtils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IRemoteData;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappedFileManager implements IMapFileController{

	private static final Logger logger = LoggerFactory.getLogger(MappedFileManager.class);
	
	private ILoaderService loaderService;
	private IRemoteDatasetService remoteService;
	private IStageScanConfiguration stageScanConfig;
	private ILiveMappingFileService liveService;
	private IRecentPlaces recentPlaces;
	
	
	public void setLoaderService(ILoaderService service) {
		this.loaderService = service;
	}
	
	public void setRemoveService(IRemoteDatasetService service) {
		this.remoteService = service;
	}
	
	public void setStageScanConfiguration(IStageScanConfiguration scanConfig) {
		this.stageScanConfig = scanConfig;
	}
	
	public void setLiveMappingService(ILiveMappingFileService lServ) {
		liveService = lServ;
	}
	
	public void setRecentPlaces(IRecentPlaces recentPlaces) {
		this.recentPlaces = recentPlaces;
	}
	
	private MappedDataArea mappedDataArea;
	private IRegistrationHelper registrationHelper;
	private IBeanBuilderHelper beanHelper;
	
	private LiveMapFileListener liveMapListener;
	
	private Set<IMapFileEventListener> listeners;

	private ExecutorService liveLoadExector;
	
	public MappedFileManager() {
		listeners = new HashSet<>();
		mappedDataArea = new MappedDataArea();
		beanHelper = new BeanBuilderWizard();
		liveLoadExector = Executors.newSingleThreadExecutor();
		
	}

	@Override
	public void removeFile(MappedDataFile file) {
		if (file == null) return;
		mappedDataArea.removeFile(file);

		fireListeners(null);
	}
	
	@Override
	public void setRegistrationHelper(IRegistrationHelper helper) {
		registrationHelper = helper;
	}
	
	@Override
	public void toggleDisplay(PlottableMapObject object) {
		boolean plotted = !object.isPlotted();
		MappedDataFile f = mappedDataArea.getParentFile(object);
		
		if (f != null) {
			Arrays.stream(f.getChildren()).filter(PlottableMapObject.class::isInstance).map(PlottableMapObject.class::cast).forEach(p -> p.setPlotted(false));
		}
		
		object.setPlotted(plotted);
		
		if (object.getRange() != null) {
			double[] range = object.getRange();
			
			Arrays.stream(mappedDataArea.getChildren())
			.filter(MappedDataFile.class::isInstance)
			.map(MappedDataFile.class::cast)
			.flatMap(file -> Arrays.stream(file.getChildren()))
			.filter(PlottableMapObject.class::isInstance)
			.map(PlottableMapObject.class::cast)
			.filter(PlottableMapObject::isPlotted)
					.filter(p -> p != object)
					.forEach(p -> {
						double[] r = p.getRange();
						if (r != null && Arrays.equals(range, r)) {
							p.setPlotted(false);
						}
					});
		}
		
		fireListeners(null);
	}
	
	

	@Override
	public void attachLive(String[] paths) {
	
		//check for live
		if (liveService != null) {
			liveMapListener = new LiveMapFileListener();
			liveService.setInitialFiles(paths);
			paths = null;
			liveService.addLiveFileListener(liveMapListener);
		}
		
	}
	

	@Override
	public List<String> loadFiles(String[] paths, IProgressService progressService) {
		
		MapLoadingRunnable runnable = new MapLoadingRunnable(paths, null, true);
		
		if (progressService == null) {
			ExecutorService ex = Executors.newSingleThreadExecutor();
			ex.submit(runnable);
			ex.shutdown();
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
	
	private void lazyAddFiles(String[] paths) {
		MappedDataFile f = null;
		for (String p : paths) {
			f = new MappedDataFile(p);
			mappedDataArea.addMappedDataFile(f);
		}
		if (f != null) fireListeners(f);
	}
	
	@Override
	public List<String> loadFile(String path, MappedDataFileBean bean, IProgressService progressService) {
		
		MapLoadingRunnable runnable = new MapLoadingRunnable(new String[] {path}, bean, true);
		
		if (progressService == null) {
			ExecutorService ex = Executors.newSingleThreadExecutor();
			ex.submit(runnable);
			ex.shutdown();
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
	
	@Override
	public void loadLiveFile(final String path, LiveDataBean bean, String parentFile, boolean lazy) {
		if (parentFile != null && !mappedDataArea.contains(parentFile)) return;
		
		LiveMapLoadingRunnable r = new LiveMapLoadingRunnable(path, bean, parentFile, lazy);
		
		liveLoadExector.submit(r);
	}
	
	@Override
	public void localReloadFile(String path, boolean force) {
		if (!mappedDataArea.contains(path)) return;
		
		MappedDataFile df = mappedDataArea.getDataFile(path);
		if (df.isEmpty() && !df.isFileFinished()) {
			df.markFileFinished();
			return;
		}
		
		if (df.isFileFinished() && !force) return;
		
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				boolean reloaded = mappedDataArea.locallyReloadLiveFile(path,loaderService);
				
				if (!reloaded) {
					try {
						IDataHolder dh = loaderService.getData(path, null);
						Tree tree = dh.getTree();
						MappedDataFileBean b = buildBeanFromTree(tree);
						
						if (b != null) {
							innerImportFile(path, b, null, null);
						}
					} catch (Exception e) {
						logger.debug("Can't automatically build bean from nexus tags",e.getMessage());
						//ignore
					}
				}
				
				fireListeners(null);
			}
		};
		
		liveLoadExector.submit(r);
	}
	
	
	@Override
	public void removeFile(String path) {
		MappedDataFile dataFile = mappedDataArea.getDataFile(path);
		removeFile(dataFile);
	}
	
	@Override
	public void clearNonLiveFiles() {
		Object[] children = mappedDataArea.getChildren();
		
		Arrays.stream(children).filter(MappedDataFile.class::isInstance)
		.map(MappedDataFile.class::cast).filter(f -> f.getLiveDataBean() == null).forEach(f -> {
			mappedDataArea.removeFile(f);
		});
		
		fireListeners(null);
	}
	
	@Override
	public boolean containsLiveFiles() {
		Object[] children = mappedDataArea.getChildren();
		
		Optional<MappedDataFile> first = Arrays.stream(children).filter(MappedDataFile.class::isInstance)
		.map(MappedDataFile.class::cast).filter(f -> f.getLiveDataBean() != null).findFirst();
		
		return first.isPresent();
	}
	
	@Override
	public void clearAll() {
		mappedDataArea.clearAll();
		fireListeners(null);
	}
	
	@Override
	public boolean contains(String path) {
		return mappedDataArea.contains(path);
	}
	
	@Override
	public List<PlottableMapObject> getPlottedObjects(){
		return mappedDataArea.getPlottedObjects();
	}
	
	private MappedDataFileBean buildBeanFromTree(Tree tree){
		MappedDataFileBean b = null;
		if (stageScanConfig == null){
			b = MapBeanBuilder.buildBean(tree);
		} else {
			String x = stageScanConfig.getActiveFastScanAxis();
			String y = stageScanConfig.getActiveSlowScanAxis();
			b = MapBeanBuilder.buildBean(tree,x,y);
		}
		return b;
	}
	
	@Override
	public MappedDataArea getArea() {
		return mappedDataArea;
	}
	
	@Override
	public void addAssociatedImage(AssociatedImage image) {
		MappedDataFile file = new MappedDataFile(image.getPath());
		file.addMapObject(image.toString(), image);
		mappedDataArea.addMappedDataFile(file);
		fireListeners(file);
	}
	
	@Override
	public void addListener(IMapFileEventListener l) {
		listeners.add(l);
	}

	@Override
	public void removeListener(IMapFileEventListener l) {
		listeners.remove(l);
	}
	
	private void fireListeners(MappedDataFile file) {
		for (IMapFileEventListener l : listeners) {
			l.mapFileStateChanged(file);
		}
	}
	
	@Override
	public void removeAllFromDisplay() {
		getPlottedObjects().stream().forEach(p -> p.setPlotted(false));
		fireListeners(null);
	}
	
	private void innerImportFile(final String path, final MappedDataFileBean bean, final IMonitor monitor, String parentPath) {
		IDataHolder dataHolder = null;
		
		if (bean.getLiveBean() != null) {
			dataHolder = remoteService.createRemoteDataHolder(path, bean.getLiveBean().getHost(), bean.getLiveBean().getPort());
		} else {
			try {
				dataHolder = loaderService.getData(path, null);
			} catch (Exception e) {
				
			}
		}
		final MappedDataFile mdf = MappedFileFactory.getMappedDataFile(path, bean, monitor,dataHolder);
		if (monitor != null && monitor.isCancelled()) return;
		if (mdf != null && parentPath != null) mdf.setParentPath(parentPath);
		mappedDataArea.addMappedDataFile(mdf);
		
		if (recentPlaces != null) {
			recentPlaces.addFiles(path);
		}
		
		fireListeners(mdf);
	}
	
	
	private class MapLoadingRunnable implements IRunnableWithProgress, Runnable {

		private String[] paths;
		private MappedDataFileBean fileBean;
		private boolean showWizard = true;
		
		public MapLoadingRunnable(String[] paths, MappedDataFileBean bean, boolean wizard) {
			this.paths = paths;
			this.fileBean = bean;
			this.showWizard = wizard;
		}
		
		@Override
		public void run() {
			run(null);
			
		}
		
		@Override
		public void run(IProgressMonitor monitor) {
			
			for (String path : paths) {
				Map<String, int[]> datasetNames = DatasetNameUtils.getDatasetInfo(path, null);
				IMetadata meta = null;
				IDataHolder dh = null;
				try {
				meta = loaderService.getMetadata(path, null);
				dh =loaderService.getData(path, null);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
				if (datasetNames != null && datasetNames.size() == 1 && datasetNames.containsKey("image-01")) {
					IDataset im = null;
					try {
						im = loaderService.getDataset(path, null);
						
					} catch (Exception e) {
						logger.error("Couldn't load data from {}", path);
					}
					
					if (registrationHelper != null && im != null) registrationHelper.register(path, im);
					
					return;
				}
				
				if (fileBean == null) {
					try {
						Tree tree = dh.getTree();
						fileBean = buildBeanFromTree(tree);
					} catch (Exception e) {
						logger.debug("Can't automatically build bean from nexus tags",e.getMessage());
						//ignore
					}
				}
				

				if (fileBean == null) fileBean = LegacyMapBeanBuilder.tryLegacyLoaders(dh);
				
				if (fileBean != null) {
					IMonitor m = null;
					if (monitor != null) {
						m = new ProgressMonitorWrapper(monitor);
						monitor.beginTask("Loading data...", -1);
					}
					
					innerImportFile(path, fileBean, m, null);
					fileBean = null;
					continue;
				}
				
				if (showWizard) {
					beanHelper.build(path, datasetNames, meta);
				} else {
					logger.error("Failed to import file :" + path);
				}
			}
			
		}
		
		public List<String> getFailedLoadingFiles() {
			return null;
		}

	}
	
	private class LiveMapLoadingRunnable implements Runnable {

		private LiveDataBean bean;
		private String path;
		private String parentFile;
		private boolean lazy = false;

		public LiveMapLoadingRunnable(final String path, LiveDataBean bean, String parentFile, boolean lazy) {
			this.path = path;
			this.bean = bean;
			this.parentFile = parentFile;
			this.lazy = lazy;
		}
		
		@Override
		public void run() {
			if (parentFile != null && !mappedDataArea.contains(parentFile)) {
				logger.error("Attempting to load already loaded live file - multiple start events?");
				return;
			}
			if (remoteService == null) {
				logger.error("Could not acquire remote dataset service");
				return;
			}
			
			if (!lazy) {
				IRemoteData rd = remoteService.createRemoteData(bean.getHost(), bean.getPort());
				
				if (rd == null) {
					logger.error("Could not acquire remote data on :" + bean.getHost() + ":" + bean.getPort());
					return;
				}
				
				try {
					rd.setPath(path);
					Map<String, Object> map = rd.getTree();
					map.toString();
					Tree tree = TreeToMapUtils.mapToTree(map, path);
					
					MappedDataFileBean buildBean = buildBeanFromTree(tree);
					
					if (buildBean != null) {
						buildBean.setLiveBean(bean);
						innerImportFile(path, buildBean, null,parentFile);
					} else {
						logger.error("Bean from live tree is null!");
					}
					
					return;
					
				} catch (Exception e) {
					//It is possible that building the live bean will fail
					logger.info("Could not build live map bean from " + path, e);
				}
			}
			
			MappedDataFile mdf = new MappedDataFile(path,bean);
			mdf.setParentPath(parentFile);
			mappedDataArea.addMappedDataFile(mdf);
			fireListeners(mdf);
			
		}
		
	}

	@Override
	public void addLiveStream(LiveStreamMapObject stream) {
		mappedDataArea.setStream(stream);
		fireListeners(null);
		
	}
	
	private class LiveMapFileListener implements ILiveMapFileListener{

		@Override
		public void fileLoadRequest(String[] path, String host, int port, String parent) {
			if (host != null) {
				LiveDataBean b = new LiveDataBean();
				b.setHost(host);
				b.setPort(port);
				for (String p : path) {
					loadLiveFile(p, b, parent, true);
				}
				
			} else {
				lazyAddFiles(path);
			}
			
			
		}

		@Override
		public void refreshRequest() {
			if (liveService != null) {
				liveService.runUpdate(() -> MappedFileManager.this.fireListeners(null), false);
			}
		}

		@Override
		public void localReload(String path, boolean force) {
			localReloadFile(path, force);
		}
		
	}
}
