package org.dawnsci.mapping.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;

public class MappedDataView extends ViewPart {
	
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
	
	@Override
	public void createPartControl(Composite parent) {
		
		area = new MappedDataArea();
		
		final IWorkbenchPage page = getSite().getPage();
		final IPlottingSystem<Composite> map;
		try {
			final IViewPart view = page.showView("org.dawnsci.mapping.ui.mapview");
			map = (IPlottingSystem<Composite>)view.getAdapter(IPlottingSystem.class);
		} catch (PartInitException e) {
			throw new RuntimeException("Could not create the map view", e);
		}
		
		final IPlottingSystem<Composite> spectrum;
		try {
			final IViewPart view = page.showView("org.dawnsci.mapping.ui.spectrumview");
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
				if (e instanceof AbstractMapData) plotManager.updateLayers((AbstractMapData)e);
				if (e instanceof AssociatedImage) plotManager.addImage((AssociatedImage)e);
				if (e instanceof MappedDataFile) {
					MappedDataFile mdf = (MappedDataFile)e;
					if (mdf.getLiveDataBean() !=null) FileManagerSingleton.getFileManager().importLiveFile(mdf.getPath(), mdf.getLiveDataBean());
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
	}
	
	private void openImportWizard(String path) {
		
		FileManagerSingleton.getFileManager().importFile(path);
		
	}
	
	@Override
	public void setFocus() {
		if (viewer != null && !viewer.getTree().isDisposed()) viewer.getTree().setFocus(); 

	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (MappedFileManager.class == adapter) return FileManagerSingleton.getFileManager();
		return super.getAdapter(adapter);
	}
	
}
