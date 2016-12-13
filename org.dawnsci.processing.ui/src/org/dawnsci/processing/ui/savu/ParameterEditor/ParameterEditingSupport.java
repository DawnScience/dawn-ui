package org.dawnsci.processing.ui.savu.ParameterEditor;

import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.richbeans.widgets.cell.FieldComponentCellEditor;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.FloatSpinnerWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Spinner;

/**
 * EditingSupport Class
 *
 */
class ParameterEditingSupport extends EditingSupport {

	private int column;
	private ParameterEditorTableViewModel viewModel;

	public ParameterEditingSupport(ParameterEditorTableViewModel viewModel, ColumnViewer viewer, int col) {
		super(viewer);
		this.column = col;
		this.viewModel = viewModel;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		System.out.println(this.getValue(element).getClass());
		FieldComponentCellEditor ed = null;
		if (this.getValue(element) instanceof Double) {
			try {

				ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
						FloatSpinnerWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			final FloatSpinnerWrapper rb = (FloatSpinnerWrapper) ed.getFieldWidget();

			rb.setFormat(rb.getWidth(), 0);
			rb.setFormat(rb.getWidth(), 3);
			rb.setMaximum(Double.MAX_VALUE);
			rb.setMinimum(-Double.MAX_VALUE);
			rb.setButtonVisible(false);
			rb.setActive(true);

			((Spinner) rb.getControl()).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						System.out.println(rb.getValue());
						setValue(element, rb.getValue(), false);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			return ed;

		}
		if (this.getValue(element) instanceof Integer) {
			try {

				ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
						FloatSpinnerWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			final FloatSpinnerWrapper rb = (FloatSpinnerWrapper) ed.getFieldWidget();

			rb.setFormat(rb.getWidth(), 0);
			rb.setFormat(rb.getWidth(), 0);
			rb.setMaximum(Integer.MAX_VALUE);
			rb.setMinimum(-Integer.MAX_VALUE);
			rb.setButtonVisible(false);
			rb.setActive(true);

			((Spinner) rb.getControl()).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						setValue(element, rb.getValue(), false);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			return ed;

		}

		if (this.getValue(element) instanceof Boolean) {
			try {

				ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
						BooleanWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			final BooleanWrapper rb = (BooleanWrapper) ed.getFieldWidget();
			
			rb.setActive(true);

			return ed;
		}

		if (this.getValue(element) instanceof String) {
			try {

				ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
						TextWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			final TextWrapper rb = (TextWrapper) ed.getFieldWidget();

			rb.setActive(true);
		}
		return ed;

	}

	@Override
	protected boolean canEdit(Object element) {
		if (column == 0)
			return false;
		if (column == 2)
			return false;
		else
			return true;
	}

	@Override
	protected Object getValue(Object element) {
		try {
			return getRowValue(element);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private Object getRowValue(Object element) throws Exception {

		final ParameterEditorRowDataModel row = (ParameterEditorRowDataModel) element;
		switch (column) {
		case 0:
			return row.getKey();
		case 1:
			return row.getValue();
		case 2:
			return row.getDescription();
		default:
			return null;
		}
	}

	@Override
	protected void setValue(Object element, Object value) {
		try {
			this.setValue(element, value, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setValue(Object element, Object value, boolean tableRefresh) throws Exception {
		final ParameterEditorRowDataModel row = (ParameterEditorRowDataModel) element;

		switch (column) {
		case 0:
			row.setKey((String) value);
			break;
		case 1:
			row.setValue(value);
			break;
		case 2:
			row.setDescription((String) value);
			break;

		default:
			break;
		}
		if (tableRefresh) {
			getViewer().refresh();
		}

		setPluginDictValues(row);


	}

	private void setPluginDictValues(ParameterEditorRowDataModel row) {
		// TODO Auto-generated method stub
		Map<String, Object> pluginDict = this.viewModel.getPluginDict();
		System.out.println(pluginDict.keySet().toString());
		System.out.println(row.getKey());
		Map<String, Object> entryGroup = (Map<String, Object>) pluginDict.get(row.getKey());
		entryGroup.put("value", row.getValue());
		pluginDict.put(row.getKey(), entryGroup);
		this.viewModel.setPluginDict(pluginDict);
		System.out.println(this.viewModel.getPluginDict());
	}
}