package org.dawnsci.mapping.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.MapObject;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileDescription;
import org.dawnsci.mapping.ui.datamodel.MappedFileFactory;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.dawnsci.mapping.ui.dialog.ImageGridDialog;
import org.dawnsci.mapping.ui.dialog.RGBMixerDialog;
import org.dawnsci.mapping.ui.dialog.RegistrationDialog;
import org.dawnsci.mapping.ui.wizards.ImportMappedDataWizard;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.ICompositeTrace;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

public class MappedDataView extends ViewPart {

	private TreeViewer viewer;
	private MappedDataArea area;
	private MapPlotManager plotManager; 
	private MappedFileManager fileManager;
	
	
	@Override
	public void createPartControl(Composite parent) {
		area = new MappedDataArea();
		
		IWorkbenchPage page = getSite().getPage();
		IViewPart view = page.findView("org.dawnsci.mapping.ui.mapview");
		IPlottingSystem map = (IPlottingSystem)view.getAdapter(IPlottingSystem.class);
		view = page.findView("org.dawnsci.mapping.ui.spectrumview");
		IPlottingSystem spectrum = (IPlottingSystem)view.getAdapter(IPlottingSystem.class);
		
		plotManager = new MapPlotManager(map, spectrum, area);
		
		map.addClickListener(new IClickListener() {
			
			@Override
			public void doubleClickPerformed(ClickEvent evt) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void clickPerformed(ClickEvent evt) {
				plotManager.plotData(evt.getxValue(), evt.getyValue());
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
				if (e instanceof MappedData) plotManager.plotMap((MappedData)e);
				viewer.refresh();
			}
		});
		
		fileManager = new MappedFileManager(plotManager, area, viewer);

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
					List<MappedData> maps = new ArrayList<MappedData>();
					while(it != null && it.hasNext()) {
						Object obj = it.next();
						if (obj instanceof MappedData) {
							maps.add((MappedData)obj);
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
						manager.add(MapActionUtils.getMapPropertiesAction(maps.get(0),plotManager, area.getDataFile(0)));
					}
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
		
		fileManager.importFile(path);
		
//		ImportMappedDataWizard wiz = new ImportMappedDataWizard(path);
//		wiz.setNeedsProgressMonitor(true);
//		final WizardDialog wd = new WizardDialog(getSite().getShell(),wiz);
//		wd.setPageSize(new Point(900, 500));
//		wd.create();
//		
//		if (wiz.isImageImport()) {
//			IDataset im;
//			try {
//				im = LocalServiceManager.getLoaderService().getDataset(path, null);
//				RegistrationDialog dialog = new RegistrationDialog(Display.getDefault().getActiveShell(), plotManager.getTopMap().getMap(),im);
//				if (dialog.open() != IDialogConstants.OK_ID) return;
//				AssociatedImage asIm = new AssociatedImage("Registered", (RGBDataset)dialog.getRegisteredImage());
//				area.getDataFile(0).addMapObject("Registered", asIm);
//				viewer.refresh();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			return;
//		}
//
//		if (wd.open() == WizardDialog.CANCEL) return;
//		
//		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(path, wiz.getMappedFileDescription());
//		area.addMappedDataFile(mdf);
//		viewer.setInput(area);
//		plotManager.clearAll();
//		plotManager.plotMap(null);
	}

	@Override
	public void setFocus() {
		if (viewer != null && !viewer.getTree().isDisposed()) viewer.getTree().setFocus(); 

	}
	
	@Override
	public Object getAdapter(Class adapter) {
		if (MappedFileManager.class == adapter) return fileManager;

		return super.getAdapter(adapter);
	}
}
