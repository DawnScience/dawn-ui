package org.dawnsci.datavis.view.parts;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.FileControllerStateEvent;
import org.dawnsci.datavis.model.FileControllerStateEventListener;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.view.Activator;
import org.dawnsci.datavis.view.perspective.DataVisPerspective;
import org.dawnsci.datavis.view.preference.DataVisPreferenceConstants;
import org.dawnsci.datavis.view.quickfile.IQuickFileWidgetListener;
import org.dawnsci.datavis.view.quickfile.QuickFileWidget;
import org.dawnsci.january.ui.utils.SelectionUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.plotting.api.PlottingEventConstants;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.StringDataset;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.utils.VersionSort;

public class LoadedFilePart {

	private static final Logger logger = LoggerFactory.getLogger(LoadedFilePart.class);
	
	private final Image icon = Activator.getImage("icons/document-block.png");
	private final Image iconLive = Activator.getImage("icons/document-light.png");
	
	private TableViewer viewer;
	
	@Inject ILoaderService lService;
	@Inject ESelectionService selectionService;
	@Inject EventAdmin eventAdmin;
	@Inject IFileController fileController;
	@Inject IRecentPlaces recentPlaces;
	
	private String partId = null;

	private Image ticked;
	private Image unticked;
	
	private FileControllerStateEventListener fileStateListener;
	
	private CompareObject sorter = null;
	
	private class QuickFileWidgetListener implements IQuickFileWidgetListener {
		@Override
		public void fileSelected(String directory, String name) {
			String match = name;
			File folder = new File(directory);
					
			// first check for presence of range
			int startRangeChar = match.indexOf('<');
			int midRangeChar = match.indexOf('-', startRangeChar + 1);
			int endRangeChar = match.indexOf('>', midRangeChar + 1);
					
			List<String> patterns = new ArrayList<>();
				
			if (startRangeChar > 0 && midRangeChar > startRangeChar && endRangeChar > midRangeChar) {
				try {
					int startRange = Integer.parseInt(match.substring(startRangeChar +1 , midRangeChar));
					int endRange = Integer.parseInt(match.substring(midRangeChar + 1, endRangeChar));
					if (endRange <= startRange)
						throw new NumberFormatException();
					String prefix = startRangeChar > 0 ? match.substring(0, startRangeChar) : "";
					String suffix = endRangeChar != match.length() - 1 ? match.substring(endRangeChar + 1): ""; 
					for (int i = startRange ; i <= endRange ; i++) {
						patterns.add(prefix + Integer.toString(i) + suffix);
					}
				} catch (NumberFormatException e) {
					patterns.add(match);
				}
			} else {
				patterns.add(match);
			}
			
			String[] names = patterns
				.stream()
				.flatMap(pattern -> Arrays.stream(folder.listFiles((FilenameFilter) new WildcardFileFilter(pattern))))
				.sorted()
				.filter(f -> !f.isDirectory())
				.map(File::getAbsolutePath)
				.toArray(String[]::new);
					
			loadData(names, true);
			logger.debug("Loaded files using quickwidget");
		}
	}

	@PostConstruct
	public void createComposite(Composite parent, MPart part) {
		partId = part.getElementId();
		logger.info("LoadedFile view: {} created", partId);
		parent.setLayout(new FormLayout());
		FormData checkForm = new FormData();
		checkForm.top = new FormAttachment(0,0);
		checkForm.left = new FormAttachment(0,0);
		checkForm.right = new FormAttachment(100,0);
		
		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(checkForm);
		
		viewer = new TableViewer(tableComposite, SWT.MULTI |SWT.FULL_SELECTION | SWT.BORDER);
		viewer.getTable().setHeaderVisible(true);
		
		QuickFileWidget qfw = new QuickFileWidget(parent);
		FormData comboForm = new FormData();
		checkForm.bottom = new FormAttachment(qfw);
		comboForm.left = new FormAttachment(0,0);
		comboForm.right = new FormAttachment(100,0);
		comboForm.bottom = new FormAttachment(100,0);
		comboForm.height = 32;
		qfw.setLayoutData(comboForm);
		qfw.setTextToolTipText("Accepts filename, directory name, .. to move back a directory,"
				+ " * as a wildcard and <x-y> for a numerical range"); 
		
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		
		boolean signalsOnly = ps.getBoolean(DataVisPreferenceConstants.SIGNALS_ONLY);
		fileController.setOnlySignals(signalsOnly);


		List<String> p = recentPlaces.getRecentDirectories();
		if (p.isEmpty()) {
			qfw.setDirectoryPath(System.getProperty("user.home"));
		} else {
			qfw.setDirectoryPath(p.get(0));
		}

		qfw.addListener(new QuickFileWidgetListener());
		
		
		ticked = AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/ticked.png").createImage();
		unticked = AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/unticked.gif").createImage();
		
		viewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				return ((IFileController)inputElement).getLoadedFiles().toArray();
			}

			@Override
			public void dispose() {
				
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				
			}
			
		});
		
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
			
			@Override
			public Image getImage(Object element) {
				
				if (element instanceof IRefreshable && ((IRefreshable)element).isLive()) {
					return iconLive;
				}
				
				return icon;
			}
			
			@Override
			public String getToolTipText(Object element) {
				if (element instanceof LoadedFile) return ((LoadedFile)element).getFilePath();
				return null;
				
			};
		});
		
		name.getColumn().setText("Filename");
		name.getColumn().setWidth(200);
		
		name.getColumn().addSelectionListener(new SelectionAdapter() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (sorter == null || !sorter.isThisColumn(name)) {
					sorter = new CompareObject(name, (LoadedFile file) -> file.getName());
				} else {
					sorter.increment();
				}
				
				viewer.getTable().setSortDirection(sorter.getDirection());
				viewer.getTable().setSortColumn(name.getColumn());
				fileController.setComparator(sorter.getComparator());
				viewer.refresh();
				
			}

		});
		
		TableViewerColumn labelColumn = new TableViewerColumn(viewer, SWT.LEFT);
		labelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				
				return ((LoadedFile)element).getLabel();
			}
		});
		
		labelColumn.getColumn().setText("Label");
		labelColumn.getColumn().setWidth(200);
		
		labelColumn.getColumn().addSelectionListener(new SelectionAdapter() {
		
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (sorter == null || !sorter.isThisColumn(labelColumn)) {
					sorter = new CompareObject(labelColumn, (LoadedFile file) -> file.getLabelValue());
				} else {
					sorter.increment();
				}
				
				viewer.getTable().setSortDirection(sorter.getDirection());
				viewer.getTable().setSortColumn(labelColumn.getColumn());
				fileController.setComparator(sorter.getComparator());
				viewer.refresh();
			}
		});
		
		LabelEditingSupport editSupport = new LabelEditingSupport(viewer);
		
		labelColumn.setEditingSupport(editSupport);
		
		TableColumnLayout columnLayout = new TableColumnLayout();
	    columnLayout.setColumnData(check.getColumn(), new ColumnPixelData(24));
	    columnLayout.setColumnData(name.getColumn(), new ColumnWeightData(100,20));
	    columnLayout.setColumnData(labelColumn.getColumn(), new ColumnWeightData(0,0));
	    
	    tableComposite.setLayout(columnLayout);
	    
		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setInput(fileController);
		
		viewer.addSelectionChangedListener(event -> {
			IStructuredSelection selection = viewer.getStructuredSelection();
//			if (selection.getFirstElement() instanceof LoadedFile) {
//			  	LoadedFile selected = (LoadedFile)selection.getFirstElement();
////			   	fileController.setCurrentFile(selected, selected.isSelected());
//			}
			    
			selectionService.setSelection(new StructuredSelection(selection.toArray()));
		});
		
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		menuMgr.addMenuListener(new LoadedFileMenuListener(fileController, viewer,editSupport));
		menuMgr.setRemoveAllWhenShown(true);
		viewer.getControl().setMenu(menu);
		
		fileStateListener = new FileControllerStateEventListener() {
			
			@Override
			public void stateChanged(FileControllerStateEvent event) {
				updateOnStateChange(event);

				String defaultDirectory = recentPlaces.getCurrentDefaultDirectory();
				if (defaultDirectory != null) {
					Display.getDefault().asyncExec(() -> qfw.setDirectoryPath(recentPlaces.getCurrentDefaultDirectory()));
				}
			}
		};
		
		fileController.addStateListener(fileStateListener);
		
		fileController.attachLive();
		
		DropTargetAdapter dropListener = new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				
				Object dropData = event.data;
				if (dropData instanceof ITreeSelection) {
					ITreeSelection selectedNode = (ITreeSelection) dropData;
					List<String> paths = new ArrayList<>();
					for (Object o : selectedNode.toArray()) {
						if (!(o instanceof IFile) && o instanceof IAdaptable) {
							o = ((IAdaptable) o).getAdapter(IFile.class);
						}
						if (o instanceof IFile) {
							IFile file = (IFile) o;
							paths.add(file.getLocation().toOSString());
						}
					}
					
					if (!paths.isEmpty()) {
						loadData(paths.toArray(new String[paths.size()]), true);
					}
					
				} else if (dropData instanceof String[]) {
					loadData((String[])dropData,true);
				} else if (dropData instanceof StructuredSelection) {
					StructuredSelection ss = (StructuredSelection)dropData;
					List<LoadedFile> lf = new ArrayList<LoadedFile>();
					
					Iterator<?> it = ss.iterator();
					
					while (it.hasNext()) {
						Object next = it.next();
						if (next instanceof LoadedFile) {
							lf.add((LoadedFile)next);
						}
					}
					
					if (!lf.isEmpty()) {
						Point p2 = new Point(event.x, event.y);
						Point p1 = viewer.getControl().toControl(p2);
						
						ViewerCell cell = viewer.getCell(p1);
						LoadedFile f = cell == null ? null : (LoadedFile)cell.getElement();
						fileController.moveBefore(lf, f);
					}
				}
			}
		};
		
		DropTarget dt = new DropTarget(viewer.getControl(), DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(dropListener);
		
		
		viewer.addDragSupport(DND.DROP_MOVE, new Transfer[] {LocalSelectionTransfer.getTransfer() }, new DragSourceListener() {
			
			@Override
			public void dragStart(DragSourceEvent event) {
				event.toString();
				
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
				StructuredSelection selection = (StructuredSelection)viewer.getSelection();
		        event.data = selection;
		        LocalSelectionTransfer.getTransfer().setSelection(selection);
			}
			
			@Override
			public void dragFinished(DragSourceEvent event) {
				
			}
		});
		
		//hook up delete key to remove from list
		viewer.getTable().addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					new LoadedFileMenuListener(fileController,viewer, null).new CloseAction(fileController,viewer).run();
				}
			}
		});
		
		if (!fileController.getLoadedFiles().isEmpty()) {
			StructuredSelection s = new StructuredSelection(fileController.getLoadedFiles().get(0));
			viewer.setSelection(s);
		}

	}
	
	private void loadData(String[] paths, boolean addToHistory) {
		List<String> loadFiles = fileController.loadFiles(paths, (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class), addToHistory);
		
		if (loadFiles != null && !loadFiles.isEmpty()) {
			MessageDialog.openError(
					Display.getCurrent().getActiveShell(),
					"File Loading Error",
					"An error occured during data loading: " + loadFiles.size() + " files could not be opened!");
		}
		
	}
	
	private void updateOnStateChange(final FileControllerStateEvent event) {
		if (Display.getCurrent() == null) {
			Display.getDefault().syncExec(() -> updateOnStateChange(event));
			return;
		}
		viewer.setInput(fileController);
		
		IStructuredSelection selection = viewer.getStructuredSelection();
		List<LoadedFile> s = SelectionUtils.getFromSelection(selection, LoadedFile.class);
		
		boolean pushSelection = event.getLoadedFile() != null || viewer.getTable().getItemCount() != 0;
		
		if (s.isEmpty() && pushSelection) {
			viewer.getTable().setSelection(0);
			selection = viewer.getStructuredSelection();
			s = SelectionUtils.getFromSelection(selection, LoadedFile.class);
		}
		
		if (!s.isEmpty()) {
			selectionService.setSelection(new StructuredSelection(selection.toArray()));
		}
	}
	
	@PreDestroy
	public void dispose() {
		fileController.removeStateListener(fileStateListener);
		fileController.detachLive();
		ticked.dispose();
		unticked.dispose();
		
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		ps.setValue(DataVisPreferenceConstants.SIGNALS_ONLY,fileController.isOnlySignals());
	}

	@Focus
	public void setFocus() {
		if (viewer != null) viewer.getControl().setFocus();
	}

	/**
	 * Override this in subclasses
	 * @return perspective that this part belongs to
	 */
	protected String getPerspectiveID() {
		return DataVisPerspective.ID;
	}

	@Inject
	@Optional
	private void subscribeFileOpenE3(@UIEventTopic(PlottingEventConstants.FILE_OPEN_EVENT) Event data, MPart part, EModelService modelService) {
		if (part == null || !part.getElementId().equals(partId)) {
			return;
		}

		MPerspective ap = modelService.getActivePerspective(modelService.getTopLevelWindowFor(part));
		if (!ap.getElementId().equals(getPerspectiveID())) {
			return;
		}
		MUIElement element = modelService.find(partId, ap);
		if (element == null) { // when active perspective does not contain this part
			return;
		}

		String[] paths = (String[]) data.getProperty(PlottingEventConstants.MULTIPLE_FILE_PROPERTY);
		if (paths == null) {
			String path = (String) data.getProperty(PlottingEventConstants.SINGLE_FILE_PROPERTY);
			paths = new String[] { path };
		}

		if (data.getProperty(PlottingEventConstants.LIVE_BEAN_PROPERTY) != null) return;

		boolean addToHistory = true;
		
		Object o = data.getProperty(PlottingEventConstants.ADD_TO_HISTORY_PROPERTY);
		
		if (o instanceof Boolean) {
			addToHistory = (Boolean)o;
		}
		
		loadData(paths, addToHistory);
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
				fileController.setFileSelected((LoadedFile)element, (Boolean)value);
			}
//			fileController().setCurrentData((DataOptions)element, (Boolean)value);
		}
		
	}

	public class LabelEditingSupport extends EditingSupport {
		
	    private TextCellEditor cellEditor;
	    private boolean canEdit = false;
	    
	    public LabelEditingSupport(ColumnViewer viewer) {
	        super(viewer);
	        cellEditor = new TextCellEditor((Composite) getViewer().getControl());
	    }
	    
	    protected CellEditor getCellEditor(Object element) {
	        return cellEditor;
	    }
	    
	    protected boolean canEdit(Object element) {
	        return canEdit;
	    }
	    
	    protected Object getValue(Object element) {
	    	
	    	if (element instanceof LoadedFile) {
	    		return ((LoadedFile)element).getLabel();
	    	}
	    	
	        return "";
	    }
	    
	    protected void setValue(Object element, Object value) {
	    	if (element instanceof LoadedFile) {
	    		((LoadedFile)element).setLabel(value.toString());
	    	}
	    	viewer.refresh();
	    }
	    
	    public boolean isCanEdit() {
	    	return canEdit;
	    }
	    
	    public void setCanEdit(boolean canEdit) {
	    	this.canEdit = canEdit;
	    }
	}
	
	private class CompareObject {
		
		private Function<? super LoadedFile, ? extends Object> comparatorFunction;
		private Comparator<LoadedFile> comparator;
		private TableViewerColumn column;
		private int direction;
		
		public CompareObject(TableViewerColumn column, Function<? super LoadedFile, ? extends Object> keyExtractor) {
			this.column = column;
			this.comparatorFunction = keyExtractor;
			this.comparator = createComparator();
			direction = SWT.DOWN;
		}
		
		public boolean isThisColumn(TableViewerColumn column) {
			return this.column == column;
		}
		
		public int getDirection() {
			return direction;
		}
		
		public Comparator<LoadedFile> getComparator(){
			return comparator;
		}
		
		public void increment() {
			switch (direction) {
			case SWT.DOWN:
				direction = SWT.UP;
				comparator = createComparator();
				comparator = comparator.reversed();
				break;
			case SWT.UP:
				direction = SWT.NONE;
				comparator = null;
				break;
			case SWT.NONE:
				direction = SWT.DOWN;
				comparator = createComparator();
				break;

			default:
				break;
			}
		}
		
		private Comparator<LoadedFile> createComparator(){
			return new Comparator<LoadedFile>() {

				@Override
				public int compare(LoadedFile o1, LoadedFile o2) {
					Object oc1 = comparatorFunction.apply(o1);
					Object oc2 = comparatorFunction.apply(o2);
					
					if (oc1 == null && oc2 == null) return 0;
					
					if (oc1 == null) return -1;
					if (oc2 == null) return 1;
					
					if (oc1 instanceof StringDataset || oc2 instanceof StringDataset) {
						oc1 = ((Dataset)oc1).getString();
						oc2 = ((Dataset)oc2).getString();
					}
					
					if (oc1 instanceof Dataset && oc2 instanceof Dataset) {
						double d1 = ((Dataset)oc1).getDouble();
						double d2 = ((Dataset)oc2).getDouble();
						return Double.compare(d1, d2);
					}
					
					if (oc1 instanceof String && oc2 instanceof String) {
						return VersionSort.versionCompare((String)oc1,(String)oc2);
					}
					
					return 0; 
				}
			};
		}
		
	}

}