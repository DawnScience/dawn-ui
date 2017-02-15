package org.dawnsci.datavis.view.parts;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.FileControllerStateEvent;
import org.dawnsci.datavis.model.FileControllerStateEventListener;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.LoadedFiles;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class LoadedFilePart {

	private TableViewer viewer;
	
	@Inject ILoaderService lService;
	@Inject ESelectionService selectionService;
	@Inject EventAdmin eventAdmin;
	
	private Image ticked;
	private Image unticked;
	
	FileControllerStateEventListener fileStateListener;

	@PostConstruct
	public void createComposite(Composite parent) {
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		parent.setLayout(fillLayout);
		
		LoadedFiles loadedFiles = FileController.getInstance().getLoadedFiles();
//		FileController.getInstance().loadFile("/home/jacobfilik/Work/data/exampleFPA.nxs");

		Composite tableComposite = new Composite(parent, SWT.NONE);
		
		
		viewer = new TableViewer(tableComposite, SWT.MULTI |SWT.FULL_SELECTION | SWT.BORDER);
//		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer.getTable().setHeaderVisible(true);
		
		ticked = AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/ticked.png").createImage();
		unticked = AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/unticked.gif").createImage();
		
		viewer.setContentProvider(new FileTreeContentProvider());
		
		TableViewerColumn check   = new TableViewerColumn(viewer, SWT.CENTER, 0);
		check.setEditingSupport(new CheckBoxEditSupport(viewer));
		check.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}
			
			@Override
			public Image getImage(Object element) {
				return ((LoadedFile)element).isSelected() ? ticked : unticked;
			}
			
		});

		check.getColumn().setWidth(28);
		
		TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT);
		name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((LoadedFile)element).getName();
			}
		});
		
		name.getColumn().setText("Filename");
		name.getColumn().setWidth(200);
		
		TableColumnLayout columnLayout = new TableColumnLayout();
	    columnLayout.setColumnData(check.getColumn(), new ColumnPixelData(24));
	    columnLayout.setColumnData(name.getColumn(), new ColumnWeightData(100,20));
	    
	    tableComposite.setLayout(columnLayout);
		
		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setInput(loadedFiles);
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			  @Override
			  public void selectionChanged(SelectionChangedEvent event) {
			    IStructuredSelection selection = viewer.getStructuredSelection();
			    if (selection.getFirstElement() instanceof LoadedFile) {
			    	LoadedFile selected = (LoadedFile)selection.getFirstElement();
			    	FileController.getInstance().setCurrentFile(selected, selected.isSelected());
			    }
			    
			    selectionService.setSelection(new StructuredSelection(selection.toArray()));
			    
			  }
			});
		
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (viewer.getSelection().isEmpty())
					return;
				if (viewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					
					manager.add(new Action("Check",AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/ticked.png")) {
						@Override
						public void run() {
							List<LoadedFile> collected = Arrays.stream(selection.toArray())
									.filter(LoadedFile.class::isInstance)
									.map(LoadedFile.class::cast)
									.collect(Collectors.toList());
							FileController.getInstance().selectFiles(collected, true);
//							List<LoadedFile> fs = FileController.getInstance().getSelectedFiles();
//							viewer.setCheckedElements(fs.toArray());
							viewer.refresh();
						}
					});
					
					manager.add(new Action("Uncheck",AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/unticked.gif")) {
						@Override
						public void run() {
							List<LoadedFile> collected = Arrays.stream(selection.toArray())
									.filter(LoadedFile.class::isInstance)
									.map(LoadedFile.class::cast)
									.collect(Collectors.toList());
							FileController.getInstance().selectFiles(collected, false);
//							List<LoadedFile> fs = FileController.getInstance().getSelectedFiles();
//							viewer.setCheckedElements(fs.toArray());
							viewer.refresh();
						}
					});
					
					manager.add(new Separator());
					
					if (selection.size() == 1 && selection.getFirstElement() instanceof LoadedFile) {

						final LoadedFile f = (LoadedFile)selection.getFirstElement();
						manager.add(new Action("Apply to all files") {
							@Override
							public void run() {
								FileController.getInstance().applyToAll(f);
							}
						});
					}
					
					manager.add(new Action("Close") {
						@Override
						public void run() {
							List<LoadedFile> collected = Arrays.stream(selection.toArray())
									.filter(LoadedFile.class::isInstance)
									.map(LoadedFile.class::cast)
									.collect(Collectors.toList());
							FileController.getInstance().unloadFiles(collected);
							viewer.refresh();
						}
					});
					
					
					
					
					
				}
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		viewer.getControl().setMenu(menu);
		
		fileStateListener = new FileControllerStateEventListener() {
			
			@Override
			public void stateChanged(FileControllerStateEvent event) {
				updateOnStateChange(event);
				
				
			}
		};
		
		FileController.getInstance().addStateListener(fileStateListener);
		
		DropTargetAdapter dropListener = new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				Object dropData = event.data;
				if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					List<String> paths = new ArrayList<String>();
					for (int i = 0; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							paths.add(file.getLocation().toOSString());
						}
					}
					
					if (!paths.isEmpty()) {
						FileController.getInstance().loadFiles(paths.toArray(new String[paths.size()]),(IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class));
					}
					
				} else if (dropData instanceof String[]) {
					FileController.getInstance().loadFiles((String[])dropData,(IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class));
				}
			}
		};
		
		DropTarget dt = new DropTarget(viewer.getControl(), DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(dropListener);
	}
	
	private void updateOnStateChange(final FileControllerStateEvent event) {
		if (Display.getCurrent() == null) {
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					updateOnStateChange(event);
				}
			});
			
			return;
		}
		
//		if (!event.isSelectedDataChanged() && !event.isSelectedFileChanged()) {
//			List<LoadedFile> fs = FileController.getInstance().getSelectedFiles();
//			viewer.setCheckedElements(fs.toArray());
//
//		}
//		viewer.setCheckedElements(new Object[]{FileController.getInstance()});
		viewer.refresh();
	}
	
	@PreDestroy
	public void dispose(){
		FileController.getInstance().removeStateListener(fileStateListener);
		ticked.dispose();
		unticked.dispose();
	}

	@Focus
	public void setFocus() {
		if (viewer != null) viewer.getControl().setFocus();
	}
	
	@Inject
	@Optional
	private void subscribeFileOpen(@UIEventTopic("orgdawnsciprototypee4nano") String path) {
	  try {
			LoadedFile f = new LoadedFile(lService.getData(path,null));
			FileController.getInstance().getLoadedFiles().addFile(f);
			viewer.refresh();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Inject
	@Optional
	private void subscribeFileOpen(@UIEventTopic("orgdawnsciprototypeplotupdate")  Event data) {
	  try {
			if (data.containsProperty("path")){
				String path = data.getProperty("path").toString();
				System.out.println(FileController.getInstance().getCurrentFile().isSelected());
				if (!FileController.getInstance().getCurrentFile().isSelected()) return;
				FileController.getInstance().getLoadedFiles().deselectOthers(path);
//				viewer.setCheckedElements(new Object[]{FileController.getInstance().getLoadedFiles().getLoadedFile(path)});
				viewer.refresh();
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Inject
	@Optional
	private void subscribeFileOpenE3(@UIEventTopic("org/dawnsci/events/file/OPEN") Event data ) {
		String[] paths = (String[])data.getProperty("paths");
		if (paths == null) {
			String path = (String)data.getProperty("path");
			paths = new String[]{path};
		}
		IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
		FileController.getInstance().loadFiles(paths,service);

	} 
	
	private class CheckBoxEditSupport extends EditingSupport {

		public CheckBoxEditSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			CheckboxCellEditor edit = new CheckboxCellEditor(viewer.getTable());
			edit.setValue(((LoadedFile)element).isSelected());
			return edit;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof LoadedFile) return ((LoadedFile)element).isSelected();
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof LoadedFile && value instanceof Boolean){
				FileController.getInstance().setCurrentFile((LoadedFile)element, (Boolean)value);
			}
//			FileController.getInstance().setCurrentData((DataOptions)element, (Boolean)value);
		}
		
	}


}