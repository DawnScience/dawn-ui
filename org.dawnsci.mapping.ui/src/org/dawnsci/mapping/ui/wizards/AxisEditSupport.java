package org.dawnsci.mapping.ui.wizards;

import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class AxisEditSupport extends EditingSupport {
	
	public AxisEditSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		ComboBoxViewerCellEditor axisEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
		axisEditor.setLabelProvider(new LabelProvider());
		axisEditor.setContentProvider(new ArrayContentProvider());
		axisEditor.setActivationStyle(ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		
		axisEditor.setInput(((Dimension)element).getAxisOptions());
		return axisEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		Dimension dim = (Dimension)element;
		return dim.getAxis();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Dimension dim = (Dimension)element;
		dim.setAxis((String)value);
		getViewer().refresh();

	}

}
