package org.dawnsci.processing.ui.model;

import org.eclipse.dawnsci.analysis.api.processing.model.ModelField;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for editing an operation model. Shows a table or other
 * relevant GUI for editing the model.
 */
public class ModelViewer {

	private static final Logger logger = LoggerFactory.getLogger(ModelViewer.class);

	private TableViewer viewer;

	public void createPartControl(Composite parent) {

		this.viewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		ColumnViewerToolTipSupport.enableFor(viewer);

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

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

		createColumns(viewer);

		viewer.getTable().addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.F1) {
					// TODO Help!
				}
				if (e.character == SWT.DEL) {
					try {
						Object ob = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
						((ModelField) ob).set(null);
						viewer.refresh(ob);
					} catch (Exception ignored) {
						// Ok delete did not work...
					}
				}
			}
		});
	}

	private void createColumns(TableViewer viewer) {
		
		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
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

		var = new TableViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Value");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new ModelFieldLabelProvider());
		var.setEditingSupport(new ModelFieldEditingSupport(viewer));
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public Control getControl() {
		return viewer.getControl();
	}

	public void refresh() {
		viewer.refresh();
	}

	public void dispose() {
	}

	public void setModelFields(ModelField... fields) {
		if (!viewer.getTable().isDisposed()) {
			viewer.setInput(fields);
		}
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
				if (inputElement instanceof ModelField[]) {
					return (ModelField[]) inputElement;
				}
				return new ModelField[0];
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
				ModelField field = (ModelField) element;
				field.set(value); // Changes model value, getModel() will now return a model with the value changed.
				viewer.refresh();
			} catch (Exception e) {
				logger.error("Could not set field value", e);
			}
		}
	}
}
