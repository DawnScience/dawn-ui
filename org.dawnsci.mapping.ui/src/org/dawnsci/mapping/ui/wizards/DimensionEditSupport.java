package org.dawnsci.mapping.ui.wizards;


import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class DimensionEditSupport extends EditingSupport {

	private ComboBoxViewerCellEditor dimensionEditor = null;
	private boolean unique = true;
	private Dimension[] dimensions;
	
	public DimensionEditSupport(ColumnViewer viewer, String[] options, Dimension[] dimensions) {
		super(viewer);
		this.dimensions = dimensions;
		dimensionEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
		dimensionEditor.setLabelProvider(new LabelProvider());
		dimensionEditor.setContentProvider(new ArrayContentProvider());
		dimensionEditor.setActivationStyle(ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		dimensionEditor.setInput(options);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return dimensionEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		Dimension dim = (Dimension)element;
		return dim.getDescription();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Dimension dim = (Dimension)element;
		String strval = (String)value;
		dim.setDescription(strval);
		
		if (unique) {
			for (Dimension d : dimensions) {
				if (d != dim && strval != null && strval.equals(d.getDescription())) {
					d.setDescription(null);
				}
			}
		}
		
		
		getViewer().refresh();
	}

}
