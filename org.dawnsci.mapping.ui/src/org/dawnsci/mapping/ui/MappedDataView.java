package org.dawnsci.mapping.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.LiveDataBean;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.dawnsci.plotting.views.ToolPageView;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.processing.IOperationBean;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
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
	public final static String ID = "org.dawnsci.mapping.ui.mappeddataview";
	
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
			// TODO: calculate lazily
			return filePath;
		}
		
	}

	public static final String EVENT_TOPIC_MAPVIEW_CLICK = "org/dawnsci/mapping/ui/mapview/click";
	
	private TreeViewer viewer;
	private MappedDataArea area;
	private MapPlotManager plotManager; 
	
	@SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {
		
		area = new MappedDataArea();
		
		final IWorkbenchPage page = getSite().getPage();
		final IPlottingSystem<Composite> map;
		try {
			final IViewPart view = page.showView(getSecondaryIdAttribute("mapview", "org.dawnsci.mapping.ui.mapview"));
			map = (IPlottingSystem<Composite>)view.getAdapter(IPlottingSystem.class);
			map.setPlotType(PlotType.IMAGE);
			// remove unwanted tool & menu contribution
			IActionBars bar = map.getActionBars();
			IToolBarManager tbm = bar.getToolBarManager();
			tbm.remove(ToolPageRole.ROLE_2D.getId());
			tbm.remove(BasePlottingConstants.SNAP_TO_GRID);
			IMenuManager mm = bar.getMenuManager();
			mm.remove(BasePlottingConstants.XY_PLOT_MENU_ID);
			mm.remove(BasePlottingConstants.IMAGE_PLOT_MENU_ID);
			mm.remove(BasePlottingConstants.IMAGE_ORIGIN_MENU_ID);
			// add histogram action
			IAction histogramAction = new Action() {
				@Override
				public void run() {
					try {
						if (map.getTraces().iterator().hasNext()) {
							final IToolPageSystem system = (IToolPageSystem) map.getAdapter(IToolPageSystem.class);
							system.setToolVisible(BasePlottingConstants.HISTO_TOOL_ID,
									ToolPageRole.ROLE_2D, ToolPageView.TOOLPAGE_2D_VIEW_ID);
						}
					} catch (Exception e1) {
						logger.error("Cannot show histogram tool programatically!", e1);
					}
				}
			};
			histogramAction.setImageDescriptor(Activator.getImageDescriptor("icons/color_wheel.png"));
			tbm.insertBefore(BasePlottingConstants.CONFIG_SETTINGS, histogramAction);
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
		
		plotManager = new MapPlotManager(map, spectrum, area);
		
		map.addClickListener(new IClickListener() {
			
			@Override
			public void doubleClickPerformed(final ClickEvent evt) {
				Map<String,Object> props = new HashMap<>();
				props.put("event", new MapClickEvent(evt, true, null));
				LocalServiceManager.getEventAdmin().postEvent(new Event(EVENT_TOPIC_MAPVIEW_CLICK, props));
			}
			
			@Override
			public void clickPerformed(final ClickEvent evt) {
				Map<String,Object> props = new HashMap<>();
				props.put("event", new MapClickEvent(evt, false, null));
				LocalServiceManager.getEventAdmin().postEvent(new Event(EVENT_TOPIC_MAPVIEW_CLICK, props));
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

		viewer = new TreeViewer(parent);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new MapFileTreeContentProvider());
		viewer.setLabelProvider(new MapFileCellLabelProvider(plotManager));
		viewer.setInput(area);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object e = ((StructuredSelection)event.getSelection()).getFirstElement();
				if (e instanceof AbstractMapData || e instanceof MappedDataBlock) plotManager.updateLayers((PlottableMapObject)e);
				if (e instanceof AssociatedImage) plotManager.addImage((AssociatedImage)e);
				if (e instanceof MappedDataFile) {
					MappedDataFile mdf = (MappedDataFile)e;
					if (mdf.getLiveDataBean() !=null) FileManagerSingleton.getFileManager().importLiveFile(mdf.getPath(), mdf.getLiveDataBean(),null);
				}
				viewer.refresh();
			}
		});
		
		FileManagerSingleton.initialiseManager(plotManager, area, viewer);

		// Add menu and action to treeviewer
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (viewer.getSelection().isEmpty())
					return;
				if (viewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					Iterator<?> it = selection.iterator();
					List<AbstractMapData> maps = new ArrayList<AbstractMapData>();
					
					if (selection.size() == 1 && selection.getFirstElement() instanceof MappedDataFile) {

						manager.add(MapActionUtils.getFileRemoveAction(FileManagerSingleton.getFileManager(), (MappedDataFile)selection.getFirstElement()));

					}
					
					if (selection.size() == 1 && selection.getFirstElement() instanceof MappedDataBlock && ((MappedDataBlock)selection.getFirstElement()).getLazy().getRank() == 3) {

						manager.add(MapActionUtils.getDynamicViewerAction((MappedDataBlock)selection.getFirstElement()));

					}
					
					if (selection.size() == 1 && selection.getFirstElement() instanceof AssociatedImage) {

						manager.add(MapActionUtils.getSaveImageAction((AssociatedImage)selection.getFirstElement()));

					}
					
					List<MappedDataFile> mdfs = new ArrayList<MappedDataFile>();
					while(it != null && it.hasNext()) {
						Object obj = it.next();
						
						if (obj instanceof MappedDataFile) mdfs.add((MappedDataFile)obj);
						
						if (obj instanceof AbstractMapData) {
							maps.add((AbstractMapData)obj);
						}
					}
					
					if (selection instanceof ITreeSelection) {
						Object ob = ((ITreeSelection)selection).getPaths()[0].getParentPath().getFirstSegment();
						if (ob instanceof MappedDataFile) {
							MappedDataFile df = (MappedDataFile)ob;
							if (!maps.isEmpty())manager.add(MapActionUtils.getRGBDialog(maps, df,viewer));
						}
					}
					
					if (!maps.isEmpty())manager.add(MapActionUtils.getComparisonDialog(maps));
					if (maps.size() == 1) {
						manager.add(MapActionUtils.getMapPropertiesAction(maps.get(0),plotManager, area));
					}
					
					if (mdfs.size() > 1) manager.add(MapActionUtils.getFilesRemoveAction(FileManagerSingleton.getFileManager(),mdfs));
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
				if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					for (int i = 0; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							openImportWizard(file.getLocation().toOSString());
							return;
						}
					}
				} else if (dropData instanceof String[]) {
					openImportWizard(((String[])dropData)[0]);

				}
				
			}
		});
		
		subscribeToOperationStatusTopic();
	}
	
	
	private void subscribeToOperationStatusTopic(){
		
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri==null) return; // Nothing to start, standard DAWN.


		// Check the service is available this should always be true!
		if (LocalServiceManager.getEventService() == null) {
			return;
		}

		try {
			final URI uri = new URI(suri);
			ISubscriber<EventListener> subscriber = LocalServiceManager.getEventService().createSubscriber(uri, "scisoft.operation.STATUS_TOPIC");
			subscriber.addListener(new IBeanListener<StatusBean>() {

				@Override
				public void beanChangePerformed(BeanEvent<StatusBean> evt) {
					System.out.println("bean update " + evt.getBean().toString());
					
					if (evt.getBean() instanceof IOperationBean && evt.getBean().getStatus().isRunning()) {
						String host = MappingScanNewStyleEventObserver.getDataServerHost();
						int port = MappingScanNewStyleEventObserver.getDataServerPort();
						final IOperationBean bean = (IOperationBean)evt.getBean();
						final LiveDataBean lb = new LiveDataBean();
						lb.setHost(host);
						lb.setPort(port);

						FileManagerSingleton.getFileManager().importLiveFile(bean.getOutputFilePath(), lb, bean.getFilePath());

						
						
					}
					
					if (!evt.getBean().getStatus().isFinal()) return;
					if (evt.getBean() instanceof IOperationBean) {
						final IOperationBean bean = (IOperationBean)evt.getBean();

						FileManagerSingleton.getFileManager().locallyReloadLiveFile(bean.getOutputFilePath());


					}
				}
			});
			
			
		} catch (URISyntaxException | EventException e) {
			
		}
		
	}
	
	private void openImportWizard(String path) {
		
		FileManagerSingleton.getFileManager().importFile(path);
		
	}
	
	@Override
	public void setFocus() {
		if (viewer != null && !viewer.getTree().isDisposed()) viewer.getTree().setFocus(); 

	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (plotManager != null)
			plotManager.stopRepeatPlot();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		if (MappedFileManager.class == adapter) return FileManagerSingleton.getFileManager();
		return super.getAdapter(adapter);
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


}
