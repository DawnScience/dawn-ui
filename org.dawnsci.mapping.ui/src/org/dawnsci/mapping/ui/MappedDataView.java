package org.dawnsci.mapping.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.IMapFileEventListener;
import org.dawnsci.mapping.ui.datamodel.IMapPlotController;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scanning.api.scan.IFilePathService;
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
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main view showing the table of files loaded into the mapping perspective.
 * <p>
 * Also forces the plot views in the mapping perspective to be created so
 * the plotting systems in the IMapPlotController are created
 */
public class MappedDataView extends ViewPart {

	private static Logger logger = LoggerFactory.getLogger(MappedDataView.class);
	public static final String ID = "org.dawnsci.mapping.ui.mappeddataview";
	
	private TreeViewer viewer;
	private MappedDataViewState initialState;
	private IMapFileEventListener mapFileListener;
	private IMapFileController fileController;
	
	@Override
	public void createPartControl(Composite parent) {
		
		fileController = Activator.getService(IMapFileController.class);
		IMapPlotController plotManager = Activator.getService(IMapPlotController.class);
		fileController.setRegistrationHelper(new RegistrationHelperImpl(plotManager));
		
		//find plotviews which force the plotting systems to be created,
		//even if the views are covered
		final IWorkbenchPage page = getSite().getPage();
		page.findView(MappingPerspective.MAPPING_PLOT_ID);
		page.findView(MappingPerspective.SPECTRUM_PLOT_ID);
		boolean initPlots = plotManager.initPlots();
		
		if (!initPlots) {
			//should happen but may not be fatal...
			logger.warn("Mapping plot systems could not be initialised");
		}
		
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
		viewer.setLabelProvider(new MapFileCellLabelProvider());
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
				if (mdf.getLiveDataBean() !=null) {
					fileController.loadLiveFile(mdf.getPath(), mdf.getLiveDataBean(),mdf.getParentPath(),false);
				} else if (mdf.isFileFinished() && mdf.getChildren().length == 0) {
					fileController.localReloadFile(mdf.getPath(), true);
				}
					
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
			
			//single selection only actions
			if (selection.size() == 1) {
				
				Object element = selection.getFirstElement();
				
				if (element instanceof LiveStreamMapObject) {
					IFilePathService s = Activator.getService(IFilePathService.class);
					//should only be in gda
					if (s.getProcessingDir() != null) {
						IAction a = MapActionUtils.getSaveStreamAction((LiveStreamMapObject)element,fileController);
						if (((LiveStreamMapObject)element).getMap() == null) {
							a.setEnabled(false);
						}
						manager.add(a);
					}
				}

				if (element instanceof MappedDataFile) {
					manager.add(MapActionUtils.getFileRemoveAction(fileController, (MappedDataFile)element));
					manager.add(MapActionUtils.getCopyPathToClipboardAction(((MappedDataFile)element).getPath()));
				}

				if (element instanceof MappedDataBlock &&
						((MappedDataBlock)element).getLazy().getRank() == 3) {
					manager.add(MapActionUtils.getDynamicViewerAction((MappedDataBlock)element));
				}

				if (element instanceof AssociatedImage) {
					manager.add(MapActionUtils.getSaveImageAction((AssociatedImage)element));
				}

				if (element instanceof PlottableMapObject && ((PlottableMapObject)element).isPlotted()) {
					PlottableMapObject p = (PlottableMapObject)element;
					manager.add(MapActionUtils.getBringToFrontAction(p, plotManager));
					manager.add(MapActionUtils.getSendToBackAction(p, plotManager));
				}
			}
				
			List<MappedDataFile> mdfs = new ArrayList<>();
			while(it != null && it.hasNext()) {
				Object obj = it.next();
					
				if (obj instanceof MappedDataFile) {
					MappedDataFile f = (MappedDataFile)obj;
					mdfs.add(f);
				}
				
				if (obj instanceof MappedDataBlock && ((MappedDataBlock)obj).getMapObject() != null) {
					maps.add(((MappedDataBlock)obj).getMapObject());
				}
					
				if (obj instanceof AbstractMapData) {
					maps.add((AbstractMapData)obj);
				}
			}
			
			//for rgb and vector need parent file
			if (selection instanceof ITreeSelection) {
				Object ob = ((ITreeSelection)selection).getPaths()[0].getParentPath().getFirstSegment();
				if (ob instanceof MappedDataFile) {
					MappedDataFile df = (MappedDataFile)ob;
					if (!maps.isEmpty())manager.add(MapActionUtils.getRGBDialog(maps, df,viewer));
					if (!maps.isEmpty())manager.add(MapActionUtils.getVectorDialog(maps, df,viewer));
				}
			}
				
			if (!maps.isEmpty()) {
				manager.add(MapActionUtils.getComparisonDialog(maps));
			}
			
			manager.add(new Separator());
			manager.add(MapActionUtils.getUnPlotAllAction(plotManager, viewer, fileController));
				
			if (!mdfs.isEmpty()) {
				manager.add(MapActionUtils.getUnPlotFromFilesAction(fileController,mdfs));
				
				manager.add(new Separator());
				MenuManager transfer = new MenuManager("Transfer");
				transfer.add(MapActionUtils.transferToDataVisAction(mdfs));
				manager.add(transfer);
				
				manager.add(new Separator());
				manager.add(MapActionUtils.getFilesRemoveAllAction(fileController));
				
				if (fileController.containsLiveFiles()) {
					manager.add(new Separator());
					manager.add(MapActionUtils.getNonLiveFilesRemoveAction(fileController));
				}
			}
			
			if (mdfs.size() > 1) {
				manager.add(new Separator());
				manager.add(MapActionUtils.getFilesRemoveAction(fileController,mdfs));
			}
			
			if (maps.size() == 1) {
				manager.add(new Separator());
				manager.add(MapActionUtils.getMapPropertiesAction(maps.get(0),plotManager, fileController));
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
		
		getSite().setSelectionProvider(viewer);
		fileController.attachLive(filesToReload);
		
	}
	
	@Override
	public void setFocus() {
		if (viewer != null && !viewer.getTree().isDisposed()) viewer.getTree().setFocus(); 

	}
	
	@Override
	public void dispose() {
		super.dispose();
		fileController.removeListener(mapFileListener);
		
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
		
		if (file != null && file.hasChildren() && viewer instanceof TreeViewer) {
			((TreeViewer)viewer).expandToLevel(file, 1);
		}
	}

}
