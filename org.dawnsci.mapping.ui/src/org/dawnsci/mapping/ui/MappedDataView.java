package org.dawnsci.mapping.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.math3.ml.clustering.FuzzyKMeansClusterer;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.IMapFileEventListener;
import org.dawnsci.mapping.ui.datamodel.LiveDataBean;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This view by default links to plotting systems on 
 * "org.dawnsci.mapping.ui.mapview" and "org.dawnsci.mapping.ui.spectrumview"
 * however a secondary id may be used when the view is opened. From a perspective
 * this id is specified colon separated from the view id. So to show a 
 * mapped data view with different linked views use:
 * <pre>
 * showView(MappedDataView.ID+":mapview=myViewForMappingID;spectrumview=myViewForSpectrumID")
 * </pre>
 * 
 * @author Jacob Filik
 * @author Matthew Gerring
 *
 */
public class MappedDataView extends ViewPart {

	private static Logger logger = LoggerFactory.getLogger(MappedDataView.class);
	public static final String ID = "org.dawnsci.mapping.ui.mappeddataview";
	
	private static class MapClickEvent implements IMapClickEvent {
		
		private final ClickEvent clickEvent;
		private final boolean isDoubleClick;
		private final String filePath;
		
		public MapClickEvent(ClickEvent clickEvent, boolean isDoubleClick,
				String filePath) {
			this.clickEvent = clickEvent;
			this.isDoubleClick = isDoubleClick;
			this.filePath = filePath;
		}

		@Override
		public ClickEvent getClickEvent() {
			return clickEvent;
		}

		@Override
		public boolean isDoubleClick() {
			return isDoubleClick;
		}

		@Override
		public String getFilePath() {
			return filePath;
		}
		
	}

	public static final String EVENT_TOPIC_MAPVIEW_CLICK = "org/dawnsci/mapping/ui/mapview/click";
	
	private TreeViewer viewer;
	private MapPlotManager plotManager;
	private MappedDataViewState initialState;
	
	private LiveMapFileListener liveMapListener;
	private IMapFileEventListener mapFileListener;
	
	private IMapFileController fileController;
	
	@SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {
		
		final IWorkbenchPage page = getSite().getPage();
		final IPlottingSystem<Composite> map;
		try {
			final IViewPart view = page.showView(getSecondaryIdAttribute("mapview", "org.dawnsci.mapping.ui.mapview"));
			map = (IPlottingSystem<Composite>)view.getAdapter(IPlottingSystem.class);
			map.setPlotType(PlotType.IMAGE);
		} catch (PartInitException e) {
			throw new RuntimeException("Could not create the map view", e);
		}
		
		final IPlottingSystem<Composite> spectrum;
		try {
			final IViewPart view = page.showView(getSecondaryIdAttribute("spectrumview", "org.dawnsci.mapping.ui.spectrumview"));
			spectrum = (IPlottingSystem<Composite>)view.getAdapter(IPlottingSystem.class);
		} catch (PartInitException e) {
			throw new RuntimeException("Could not create the spectrum view", e);
		}
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		fileController = bundleContext.getService(bundleContext.getServiceReference(IMapFileController.class));
		plotManager = new MapPlotManager(map, spectrum);
		fileController.setRegistrationHelper(new RegistrationHelperImpl(plotManager));
		
		map.addClickListener(new IClickListener() {
			
			@Override
			public void doubleClickPerformed(final ClickEvent evt) {
				sendEvent(evt,true);
			}
			
			@Override
			public void clickPerformed(final ClickEvent evt) {
				sendEvent(evt,false);
			}
			
			private void sendEvent(final ClickEvent evt, boolean isDoubleClick) {
				Map<String,Object> props = new HashMap<>();
				PlottableMapObject topMap = plotManager.getTopMap();
				String path = topMap == null ? null : topMap.getPath();
				MappedDataFile p = fileController.getArea().getParentFile(topMap);
				if (p != null && p.getParentPath() != null) {
					path = p.getParentPath();
				}
				props.put("event", new MapClickEvent(evt, isDoubleClick, path));
				
				BundleContext bundleContext =
		                FrameworkUtil.
		                getBundle(this.getClass()).
		                getBundleContext();
				
				EventAdmin a = bundleContext.getService(bundleContext.getServiceReference(EventAdmin.class));
				
				a.postEvent(new Event(EVENT_TOPIC_MAPVIEW_CLICK, props));
			}
		});
		
		map.addClickListener(new IClickListener() {
			
			@Override
			public void doubleClickPerformed(ClickEvent evt) {
				//No double click action
			}
			
			@Override
			public void clickPerformed(ClickEvent evt) {
				if (evt.isShiftDown()) {
					plotManager.plotDataWithHold(evt.getxValue(), evt.getyValue());
				}
				else {
					plotManager.plotData(evt.getxValue(), evt.getyValue());
				}
			}
		});
	
		final Composite searchComposite = new Composite(parent, SWT.NONE);
		searchComposite.setLayout(new GridLayout(2, false));
		final Label searchLabel = new Label(searchComposite, SWT.NONE);
		searchLabel.setText("Search: ");
		searchLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		final Text searchText = new Text(searchComposite, SWT.BORDER | SWT.SEARCH);
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		viewer = new TreeViewer(searchComposite);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		viewer.setContentProvider(new MapFileTreeContentProvider());
		viewer.setLabelProvider(new MapFileCellLabelProvider(plotManager));
		viewer.setInput(fileController.getArea());
		ColumnViewerToolTipSupport.enableFor(viewer);
		MappedDataFilter filter = new MappedDataFilter();
		viewer.addFilter(filter);
		searchText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				filter.setSearchText(searchText.getText());
				viewer.refresh();
			}
		});
	
		viewer.addDoubleClickListener(event -> {
			
			Object e = ((StructuredSelection)event.getSelection()).getFirstElement();
			if (e instanceof AbstractMapData || e instanceof MappedDataBlock || e instanceof AssociatedImage || e instanceof LiveStreamMapObject) {
				if (e instanceof MappedDataBlock && !((MappedDataBlock)e).canPlot()) {
					return;
				}
				
				fileController.toggleDisplay((PlottableMapObject)e);
			}

			if (e instanceof MappedDataFile) {
				MappedDataFile mdf = (MappedDataFile)e;
				if (mdf.getLiveDataBean() !=null)
					fileController.loadLiveFile(mdf.getPath(), mdf.getLiveDataBean(),mdf.getParentPath());
			}
			viewer.refresh();
		});
		
		mapFileListener = new IMapFileEventListener() {
			
			@Override
			public void mapFileStateChanged(MappedDataFile file) {
				if (file == null) {
					
				}
				
				updateViewer(file);
			}
		};
		
		fileController.addListener(mapFileListener);

		// Add menu and action to treeviewer
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		menuMgr.addMenuListener(manager -> {
			if (viewer.getSelection().isEmpty())
				return;
			if(!IStructuredSelection.class.isInstance(viewer.getSelection()))
				return;
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			Iterator<?> it = selection.iterator();
			List<AbstractMapData> maps = new ArrayList<>();
				
			if (selection.size() == 1 && selection.getFirstElement() instanceof MappedDataFile) {

				manager.add(MapActionUtils.getFileRemoveAction(fileController, (MappedDataFile)selection.getFirstElement()));
			}
				
			if (selection.size() == 1 && selection.getFirstElement() instanceof MappedDataBlock && ((MappedDataBlock)selection.getFirstElement()).getLazy().getRank() == 3) {
				manager.add(MapActionUtils.getDynamicViewerAction((MappedDataBlock)selection.getFirstElement()));
			}
				
			if (selection.size() == 1 && selection.getFirstElement() instanceof AssociatedImage) {
				manager.add(MapActionUtils.getSaveImageAction((AssociatedImage)selection.getFirstElement()));
			}
				
			List<MappedDataFile> mdfs = new ArrayList<>();
			while(it != null && it.hasNext()) {
				Object obj = it.next();
					
				if (obj instanceof MappedDataFile) {
					MappedDataFile f = (MappedDataFile)obj;
					mdfs.add(f);
				}
					
				if (obj instanceof AbstractMapData) {
					maps.add((AbstractMapData)obj);
				}
			}
			
			if (selection instanceof ITreeSelection) {
				Object ob = ((ITreeSelection)selection).getPaths()[0].getParentPath().getFirstSegment();
				if (ob instanceof MappedDataFile) {
					MappedDataFile df = (MappedDataFile)ob;
					if (!maps.isEmpty())manager.add(MapActionUtils.getRGBDialog(maps, df,viewer));
					if (!maps.isEmpty())manager.add(MapActionUtils.getVectorDialog(maps, df,viewer));
				}
			}
				
			if (!maps.isEmpty())manager.add(MapActionUtils.getComparisonDialog(maps));
			if (maps.size() == 1) {
				manager.add(MapActionUtils.getMapPropertiesAction(maps.get(0),plotManager, fileController.getArea()));
			}
				
			if (mdfs.size() > 1) manager.add(MapActionUtils.getFilesRemoveAction(fileController,mdfs));
				
			if (!maps.isEmpty()) {
				manager.add(new Separator());
				manager.add(MapActionUtils.getUnPlotAllAction(plotManager, viewer, fileController));
			}
					
			if (!mdfs.isEmpty()) {
				manager.add(new Separator());
				manager.add(MapActionUtils.getFilesRemoveAllAction(fileController));
				
				if (fileController.containsLiveFiles()) {
					manager.add(new Separator());
					manager.add(MapActionUtils.getNonLiveFilesRemoveAction(fileController));
				}
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		viewer.getControl().setMenu(menu);
		DropTarget dt = new DropTarget(viewer.getControl(), DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(), ResourceTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(new DropTargetAdapter() {

			@Override
			public void drop(DropTargetEvent event) {
				Object dropData = event.data;

				List<String> files = new ArrayList<>();

				if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object[] obj = selectedNode.toArray();
					for (int i = 0; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							files.add(file.getLocation().toOSString());
							return;
						}
					}
				} else if (dropData instanceof String[]) {
					String[] d = (String[])dropData;
					for (String s : d) {
						files.add(s);
					}

				}
				fileController.loadFiles(files.toArray(new String[files.size()]), null);
			}
		});
		
		String[] filesToReload = null;
		
		// Restore state of view
		if (initialState != null) {
			List<String> filesInView = initialState.getFilesInView();
			filesToReload = filesInView.toArray(new String[filesInView.size()]);
		}
		
		ILiveMappingFileService liveService = LiveServiceManager.getLiveMappingFileService();
		
		//check for live
		if (liveService != null) {
			liveMapListener = new LiveMapFileListener();
			liveService.setInitialFiles(filesToReload);
			filesToReload = null;
			liveService.addLiveFileListener(liveMapListener);
		}

		// Restore state of view
		if (filesToReload != null) {
			logger.info("Loading view state: {}", initialState);
			final IMapFileController mappedFileManager = fileController;
//			for (String f : filesToReload) {
				mappedFileManager.loadFiles(filesToReload, null);
//			}
		}
	}
	
//	private void openImportWizard(String path) {
//		
//		fileController.importFile(path);
//		
//	}
	
	@Override
	public void setFocus() {
		if (viewer != null && !viewer.getTree().isDisposed()) viewer.getTree().setFocus(); 

	}
	
	@Override
	public void dispose() {
		super.dispose();
		fileController.removeListener(mapFileListener);
		FileManagerSingleton.clearManager();
		
		ILiveMappingFileService liveService = LiveServiceManager.getLiveMappingFileService();
		
		if (liveService != null && liveMapListener != null) {
			liveService.removeLiveFileListener(liveMapListener);
		}
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		if (MappedFileManager.class == adapter) return fileController;
		return super.getAdapter(adapter);
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null && isRunningInGDA()) {
			try {
				final String savedState = memento.getString(getSavedStateKey());
				if (savedState != null) {
					BundleContext bundleContext =
			                FrameworkUtil.
			                getBundle(this.getClass()).
			                getBundleContext();
					
					IMarshallerService m = bundleContext.getService(bundleContext.getServiceReference(IMarshallerService.class));
					
					initialState = m.unmarshal(savedState,
							MappedDataViewState.class);
				}
			} catch (Exception e) {
				logger.error("Cannot restore view state", e);
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null && isRunningInGDA()) {
			try {
				final int numFiles = fileController.getArea().count();
				if (numFiles > 0) {
					final List<String> filesInView = new ArrayList<>();
					for (int i = 0; i < numFiles; i++) {
						filesInView.add(fileController.getArea().getDataFile(i).getPath());
					}
					final MappedDataViewState state = new MappedDataViewState();
					state.setFilesInView(filesInView);
					logger.info("Saving view state: {}", state);

					BundleContext bundleContext =
			                FrameworkUtil.
			                getBundle(this.getClass()).
			                getBundleContext();
					
					IMarshallerService m = bundleContext.getService(bundleContext.getServiceReference(IMarshallerService.class));
					
					final String stateString = m.marshal(state);
					memento.putString(getSavedStateKey(), stateString);
				}
			} catch (Exception e) {
				logger.error("Cannot save view state", e);
			}
		}
		super.saveState(memento);
	}

	private boolean isRunningInGDA() {
		// gda.var should always be set to something, so it is a reliable test
		// of whether we are running in GDA.
		return System.getProperty("GDA/gda.var") != null;
	}

	private String getSavedStateKey() {
		return ID + ".viewstate";
	}

	protected Properties                        idProperties;
	
	protected String getSecondaryIdAttribute(String key, String defaultValue) {
		String attr = getSecondaryIdAttribute(key);
		if (attr == null) return defaultValue;
		return attr;
	}

	protected String getSecondaryIdAttribute(String key) {
		if (idProperties!=null) return idProperties.getProperty(key);
		if (getViewSite()==null) return null;
		final String secondId = getViewSite().getSecondaryId();
		if (secondId == null) return null;
		idProperties = parseString(secondId);
		return idProperties.getProperty(key);
	}
	/**
	 * String to be parsed to properties. In the form of key=value pairs
	 * separated by semi colons. You may not use semi-colons in the 
	 * keys or values. Keys and values are trimmed so extra spaces will be
	 * ignored.
	 * 
	 * @param secondId
	 * @return map of values extracted from the 
	 */
	protected static Properties parseString(String properties) {
		
		if (properties==null) return new Properties();
		Properties props = new Properties();
		final String[] split = properties.split(";");
		for (String line : split) {
			final String[] kv = line.split("=", 2);
			props.setProperty(kv[0].trim(), kv[1].trim());
		}
		return props;
	}
	
	private class LiveMapFileListener implements ILiveMapFileListener{

		@Override
		public void fileLoadRequest(String path, String host, int port, String parent) {
			if (host != null) {
				LiveDataBean b = new LiveDataBean();
				b.setHost(host);
				b.setPort(port);
				fileController.loadLiveFile(path, b, parent);
			} else {
				fileController.loadFiles(new String[] {path}, null);
			}
			
			
		}

		@Override
		public void refreshRequest() {
			plotManager.updatePlot();
		}

		@Override
		public void localReload(String path) {
			fileController.localReloadFile(path);
		}
		
	}

	private void updateViewer(final MappedDataFile file) {
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				
				@Override
				public void run() {
					updateViewer(file);
				}
			});
			return;
		}
		
		viewer.refresh();
		
		if (file != null && viewer instanceof TreeViewer) {
			((TreeViewer)viewer).expandToLevel(file, 1);
		}
	}

}
