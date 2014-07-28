package org.dawnsci.common.widgets.table;

import java.util.Collection;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * This class is a table of a series of operations.
 * 
 * The widget provides and add button which when pressed can add another
 * item into the table. You may extend this table to provide for instance
 * a table which edits series of mathematical functions.
 * 
 * @author fcp94556
 *
 */
public class SeriesTable {

	protected TableViewer tableViewer;
	private SeriesEditingSupport editingSupport;
	
	/**
	 * Create the control for the table. The icon provider is checked for label and
	 * icon for the first, name column in the table. It must provide at least an
	 * icon for a given SeriesItem implementation. If it does not provide a label 
	 * then the 'getName()' method of SeriesItem is used.
	 * 
	 * @param parent       - the SWT composite to add the table to.
	 * @param iconProvider - a provider which must at least give the icon for a given SeriesItem
	 */
	public void createControl(Composite parent, IStyledLabelProvider iconProvider) {
		
		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);

		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer, new FocusCellOwnerDrawHighlighter(tableViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(tableViewer) {
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

		TableViewerEditor.create(tableViewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);


		tableViewer.setContentProvider(new SeriesContentProvider());

		createColumns(iconProvider);
	}
	
	/**
	 * This method is called to create the columns of the table.
	 * The columns required may be overridden depending on the 
	 * objects defined by setInput(...) to provide additional 
	 * columns which are optionally editable.
	 * 
	 */
	protected void createColumns(IStyledLabelProvider iconProv) {
		createNameColumn("Name", iconProv);
	}

	/**
	 * May be null or empty array. Must be called to set content provider on table.
	 * Should be called after createControl(...)
	 * 
	 * @param input
	 */
	public void setInput(Collection<ISeriesItemDescriptor> currentItems, ISeriesItemDescriptorProvider content) {
		
		tableViewer.setInput(currentItems);
		editingSupport.setSeriesItemDescriptorProvider(content);
	}

	/**
	 * 
	 * @param name
	 * @param prov
	 */
	protected void createNameColumn(final String name, final IStyledLabelProvider delegate) {
		
		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		nameColumn.getColumn().setWidth(250);
		nameColumn.getColumn().setMoveable(true);
		nameColumn.getColumn().setText(name);
		
		nameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new SeriesItemLabelProvider(delegate)));

		this.editingSupport = new SeriesEditingSupport(tableViewer, new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((ISeriesItemDescriptor)element).getDescription();
			}
			public Image getImage(Object element) {
				return delegate.getImage(element);
			}
		});
		nameColumn.setEditingSupport(editingSupport);

	}

	
	public void dispose() {
		tableViewer.getTable().dispose();
	}
}
