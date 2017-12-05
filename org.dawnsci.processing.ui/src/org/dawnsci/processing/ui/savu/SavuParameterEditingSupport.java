package org.dawnsci.processing.ui.savu;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EditingSupport Class
 *
 */
class SavuParameterEditingSupport extends EditingSupport {

	private int column;
	private SavuParameterEditorTableViewModel viewModel;
	private final static Logger logger = LoggerFactory.getLogger(SavuParameterEditingSupport.class);

	public SavuParameterEditingSupport(SavuParameterEditorTableViewModel viewModel, ColumnViewer viewer, int col) {
		super(viewer);
		this.column = col;
		this.viewModel = viewModel;				

	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		FieldComponentCellEditor ed = null;
		if (this.getValue(element) instanceof Double) {
			ed = createDoubleEditor(element, ed);
			return ed;

		}
		if (this.getValue(element) instanceof Integer) {
			ed = createIntegerEditor(element, ed);
			return ed;

		}

		if (this.getValue(element) instanceof Boolean) {
			ed = createBooleanEditor(ed);
			return ed;
		}

		if (this.getValue(element) instanceof String) {

			String theString = (String) this.getValue(element);
			Integer numSlashes = StringUtils.countMatches(theString, "/");
			Integer numDots = StringUtils.countMatches(theString, ".");
			// At some point I will use the above to make this a file browser if I think it's a path
			ed = createStringEditor(ed);
			}
		return ed;

	}


	private FieldComponentCellEditor createStringEditor(FieldComponentCellEditor ed) {
		try {
			ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
					TextWrapper.class.getName(), SWT.RIGHT);
		} catch (ClassNotFoundException e) {
			logger.error("Could not create string editor!",e);
			return null;
		}

		final TextWrapper rb = (TextWrapper) ed.getFieldWidget();

		rb.setActive(true);
		return ed;
	}

	private FieldComponentCellEditor createBooleanEditor(FieldComponentCellEditor ed) {
		try {

			ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
					BooleanWrapper.class.getName(), SWT.RIGHT);
		} catch (ClassNotFoundException e) {
			logger.error("Could not create boolean editor!",e);
			return null;
		}

		final BooleanWrapper rb = (BooleanWrapper) ed.getFieldWidget();
		
		rb.setActive(true);
		return ed;
	}

	private FieldComponentCellEditor createIntegerEditor(final Object element, FieldComponentCellEditor ed) {
		try {

			ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
					FloatSpinnerWrapper.class.getName(), SWT.RIGHT);
		} catch (ClassNotFoundException e) {
			logger.error("Could not create integer editor!",e);
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
					logger.error("Could not set integer value!",e);
				}
			}
		});
		return ed;
	}

	private FieldComponentCellEditor createDoubleEditor(final Object element, FieldComponentCellEditor ed) {
		try {

			ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
					FloatSpinnerWrapper.class.getName(), SWT.RIGHT);
		} catch (ClassNotFoundException e) {
			logger.error("Could not create double editor!",e);
			return null;
		}

		final FloatSpinnerWrapper rb = (FloatSpinnerWrapper) ed.getFieldWidget();

		rb.setFormat(rb.getWidth(), 0);
		rb.setFormat(rb.getWidth(), 2);
		rb.setMaximum(Double.MAX_VALUE);
		rb.setMinimum(-Double.MAX_VALUE);
		rb.setButtonVisible(false);
		rb.setActive(true);

		((Spinner) rb.getControl()).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					setValue(element, rb.getValue(), false);
				} catch (Exception e1) {
					logger.error("Could not set double value!",e1);;
				}
			}
		});
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
			logger.error("Could not set row value!",e);
			return null;
		}
	}

	private Object getRowValue(Object element) throws Exception {

		final SavuParameterEditorRowDataModel row = (SavuParameterEditorRowDataModel) element;
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
			logger.debug("Couldn't set value:",e);
		}
	}

	private void setValue(Object element, Object value, boolean tableRefresh) throws Exception {
		final SavuParameterEditorRowDataModel row = (SavuParameterEditorRowDataModel) element;

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

	private void setPluginDictValues(SavuParameterEditorRowDataModel row) {
		Map<String, Object> pluginDict = this.viewModel.getPluginDict();
		Map<String, Object> entryGroup = (Map<String, Object>) pluginDict.get(row.getKey());
		entryGroup.put("value", row.getValue());
		pluginDict.put(row.getKey(), entryGroup);
		this.viewModel.setPluginDict(pluginDict);
	}
}