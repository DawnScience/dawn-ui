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
import org.dawnsci.mapping.ui.AcquisitionServiceManager;
import org.dawnsci.mapping.ui.BeanBuilderWizard;
import org.dawnsci.mapping.ui.IBeanBuilderHelper;
import org.dawnsci.mapping.ui.IRegistrationHelper;
import org.dawnsci.mapping.ui.LocalServiceManager;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.wizards.LegacyMapBeanBuilder;
import org.dawnsci.mapping.ui.wizards.MapBeanBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.tree.TreeToMapUtils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IRemoteData;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappedFileManager implements IMapFileController{

	private final static Logger logger = LoggerFactory.getLogger(MappedFileManager.class);
	
	private MappedDataArea mappedDataArea;
	private IRegistrationHelper registrationHelper;
	private IBeanBuilderHelper beanHelper;
	
	private Set<IMapFileEventListener> listeners;
	
	public MappedFileManager() {
		listeners = new HashSet<>();
		mappedDataArea = new MappedDataArea();
		beanHelper = new BeanBuilderWizard();
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
	public void loadLiveFile(final String path, LiveDataBean bean, String parentFile) {
		if (parentFile != null && !mappedDataArea.contains(parentFile)) return;
		
		LiveMapLoadingRunnable r = new LiveMapLoadingRunnable(path, bean, parentFile);
		
		ExecutorService ex = Executors.newSingleThreadExecutor();
		ex.submit(r);
		ex.shutdown();
	}
	
	@Override
	public void localReloadFile(String path) {
		if (!mappedDataArea.contains(path)) return;
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				boolean reloaded = mappedDataArea.locallyReloadLiveFile(path);
				
				if (!reloaded) {
					try {
						IDataHolder dh = LocalServiceManager.getLoaderService().getData(path, null);
						Tree tree = dh.getTree();
						MappedDataFileBean b = buildBeanFromTree(tree);
						
						if (b != null) {
							mappedDataArea.removeFile(path);
							innerImportFile(path, b, null, null);
						}
					} catch (Exception e) {
						logger.debug("Can't automatically build bean from nexus tags",e.getMessage());
						//ignore
					}
				}
			}
		};
		ExecutorService ex = Executors.newSingleThreadExecutor();
		ex.submit(r);
		ex.shutdown();
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
		if (AcquisitionServiceManager.getStageConfiguration()==null){
			b = MapBeanBuilder.buildBean(tree);
		} else {
			String x = AcquisitionServiceManager.getStageConfiguration().getActiveFastScanAxis();
			String y = AcquisitionServiceManager.getStageConfiguration().getActiveSlowScanAxis();
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
	

	public PlottableMapObject getTopMap() {
		return null;

	}
	
	private void innerImportFile(final String path, final MappedDataFileBean bean, final IMonitor monitor, String parentPath) {
		final MappedDataFile mdf = MappedFileFactory.getMappedDataFile(path, bean, monitor);
		if (monitor != null && monitor.isCancelled()) return;
		if (mdf != null && parentPath != null) mdf.setParentPath(parentPath);
		mappedDataArea.addMappedDataFile(mdf);
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
				meta = LocalServiceManager.getLoaderService().getMetadata(path, null);
				dh = LocalServiceManager.getLoaderService().getData(path, null);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
				if (datasetNames != null && datasetNames.size() == 1 && datasetNames.containsKey("image-01")) {
					IDataset im = null;
					try {
						im = LocalServiceManager.getLoaderService().getDataset(path, null);
						
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

		public LiveMapLoadingRunnable(final String path, LiveDataBean bean, String parentFile) {
			this.path = path;
			this.bean = bean;
			this.parentFile = parentFile;
		}
		
		@Override
		public void run() {
			if (parentFile != null && !mappedDataArea.contains(parentFile)) return;
			IRemoteDatasetService rds = LocalServiceManager.getRemoteDatasetService();
			if (rds == null) {
				logger.error("Could not acquire remote dataset service");
				return;
			}
			
			IRemoteData rd = rds.createRemoteData(bean.getHost(), bean.getPort());
			
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
			
			
			MappedDataFile mdf = new MappedDataFile(path,bean);
			mdf.setParentPath(parentFile);
			mappedDataArea.addMappedDataFile(mdf);
			fireListeners(null);
			
		}
		
		
		
	}
}
