package org.dawnsci.processing.ui.model;

import java.util.Collection;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelField;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelUtils;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for editing an operation model. Shows a table or other
 * relevant GUI for editing the model.
 * 
 * This class simply listens to the current selection and shows a GUI for editing
 * it if the selection is an IOperation.
 * 
 * You can also call setOperation(...) to programmatically set the editing operation.
 * 
 * @author Matthew Gerring
 *
 */
public class OperationModelViewer implements ISelectionListener, ISelectionChangedListener, ISelectionProvider {

	private static final Logger logger = LoggerFactory.getLogger(OperationModelViewer.class);
	
	private TableViewer           viewer;
	private IOperationModel       model;
	private boolean showAllFields = false;

	public OperationModelViewer() {
		this(true);
	}
	
	public OperationModelViewer(boolean addListener) {
		this(addListener ? EclipseUtils.getPage() : null);
	}
	
	public OperationModelViewer(IWorkbenchPage page) {
		super();
		if (page != null) page.addSelectionListener(this);
	}
	

	public void createPartControl(Composite parent) {
		Composite tableComposite = new Composite(parent, SWT.NONE);
		
 		this.viewer = new TableViewer(tableComposite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		viewer.setContentProvider(createContentProvider());
		ColumnViewerToolTipSupport.enableFor(viewer);
		
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.setFilters(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (showAllFields) {
					return true;
				}

				ModelField field = (ModelField) element;
				OperationModelField ann = field.getAnnotation();
				return ann == null || !ann.expertOnly();
			}
		});

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				// TODO see AbstractComboBoxCellEditor for how list is made visible
				return super.isEditorActivationEvent(event)
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.keyCode == KeyLookupFactory
								.getDefault().formalKeyLookup(
										IKeyLookup.ENTER_NAME)));
			}
		};

		TableViewerEditor.create(viewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);


		TableColumnLayout columnLayout = new TableColumnLayout();
		createColumns(viewer,columnLayout);
		
		
		createDropTarget(viewer);

		viewer.getTable().addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.F1) {
					// TODO Help!
				}
				if (e.character == SWT.DEL) {
					try {
						Object ob = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
						((ModelField)ob).set(null);
						viewer.refresh(ob);
					} catch (Exception ignored) {
						// Ok delete did not work...
					}

				}
			}
		});

		viewer.addSelectionChangedListener(this);
		tableComposite.setLayout(columnLayout);
	}

	/**
	 * @param showAllFields if true then show all fields
	 */
	public void setShowAllFields(boolean showAllFields) {
		this.showAllFields = showAllFields;
		if (viewer != null) {
			viewer.refresh();
		}
	}

	private void createDropTarget(TableViewer viewer) {
		
		final Table table = (Table)viewer.getControl();

		// Create drop target for file paths.
		DropTarget target = new DropTarget(table, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] {fileTransfer, textTransfer};
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			
			private boolean checkLocation(DropTargetEvent event) {
				
				if (event.item==null || !(event.item instanceof Item)) {
					return false;
				}
				
				Item item = (Item)event.item;
				
				// will accept text but prefer to have files dropped
				Rectangle bounds = ((TableItem)item).getBounds(1);
				Point coordinates = new Point(event.x, event.y);
				coordinates = table.toControl(coordinates);
				if (!bounds.contains(coordinates)) {
					return false;
				}
				return true;
			}

			public void drop(DropTargetEvent event) {		
				
				String path = null;
				if (textTransfer.isSupportedType(event.currentDataType)) {
					path = (String)event.data;
				}
				if (fileTransfer.isSupportedType(event.currentDataType)){
					String[] files = (String[])event.data;
					path = files[0];
				}
				if (path==null) return;
				
				if (!checkLocation(event)) return;
				
				TableItem item = (TableItem)event.item;
				
				ModelField field = (ModelField)item.getData();				
				if (field!=null) {
					if (field.isFileProperty()) {
						try {
							field.set(path);
							refresh();
						} catch (Exception e) {
							logger.error("Could not set field value", e);
						}
					}
				}
			}
		});
	}

	private void createColumns(TableViewer viewer, TableColumnLayout columnLayout) {
		
        TableViewerColumn var   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new EnableIfColumnLabelProvider() {
			public String getText(Object element) {
				return ((ModelField)element).getDisplayName();
			}

			@Override
			public String getToolTipText(Object element) {
				return ((ModelField)element).getDescription();
			}
		});
		
		columnLayout.setColumnData(var.getColumn(), new ColumnWeightData(25,20));
		
		var   = new TableViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Value");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new ModelFieldLabelProvider());
		var.setEditingSupport(new ModelFieldEditingSupport(viewer));
		
		columnLayout.setColumnData(var.getColumn(), new ColumnWeightData(75,20));
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	public void refresh() {
		viewer.refresh();
	}
	
	public void dispose() {
		viewer.removeSelectionChangedListener(this);
		if (EclipseUtils.getPage()!=null) EclipseUtils.getPage().removeSelectionListener(this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object ob = ((IStructuredSelection)selection).getFirstElement();
			if (ob instanceof ISeriesItemDescriptor) {
				try {
					ob = ((ISeriesItemDescriptor) ob).getSeriesObject();
					if (ob instanceof IOperation<?, ?>) {
						setOperation((IOperation<? , ?>) ob);
					} else {
						setOperation(null);
					}
				} catch (Exception e) {
					setOperation(null);
				}
			}
		}
	}
	
	/**
	 * Specifically set the operation we would like to edit
	 * @param des
	 */
	public void setOperation(IOperation<? extends IOperationModel, ? extends OperationData> des) {
		if (viewer.getTable().isDisposed()) return;
		viewer.setInput(des);
		if (des == null) return;
		this.model = des.getModel();
		
	}

	public void setModel(IOperationModel model) {
		this.model = model;
		viewer.setInput(model);
	}
	
	public IOperationModel getModel() {
		return model;
	}
	
	private IContentProvider createContentProvider() {
		return new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

			}

			@Override
			public Object[] getElements(Object inputElement) {
				
				IOperationModel model = null;
				if (inputElement instanceof IOperation) {
					IOperation<?, ?> op = (IOperation<?, ?>) inputElement;
					model = op.getModel();
				} else if (inputElement instanceof IOperationModel) {
					model = (IOperationModel) inputElement;
				}
				try {
					final Collection<ModelField>  col = ModelUtils.getModelFields(model);
					return col.toArray(new ModelField[col.size()]);
				} catch (Exception ne) {
					return new ModelField[]{};
				}
			}
		};
	}

	
	class ModelFieldEditingSupport extends EditingSupport {

		public ModelFieldEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return ModelFieldEditors.createEditor((ModelField)element, viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((ModelField)element).get();
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				ModelField field = (ModelField)element;
				field.set(value); // Changes model value, getModel() will now return a model with the value changed.
				viewer.refresh();
			} catch (Exception e) {
				logger.error("Could not set field value", e);
			}
		}

	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		// do nothing
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.addSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return viewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener( ISelectionChangedListener listener) {
		viewer.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		viewer.setSelection(selection);
	}

}
