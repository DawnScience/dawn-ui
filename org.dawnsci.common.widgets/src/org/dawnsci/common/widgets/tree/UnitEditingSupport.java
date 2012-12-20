package org.dawnsci.common.widgets.tree;

import javax.measure.quantity.Quantity;

import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class UnitEditingSupport extends EditingSupport {

	private ColumnViewer viewer;

	public UnitEditingSupport(ColumnViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		if (element instanceof NumericNode) {
			NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			final CComboCellEditor cce = new CComboCellEditor((Composite)viewer.getControl(), node.getUnitsString(), SWT.READ_ONLY);
			cce.getCombo().addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setValue(element, cce.getValue());
				}
			});
			return cce;
		}
		return null;
	}

	@Override
	protected boolean canEdit(Object element) {
		if (!(element instanceof NumericNode)) return false;
		NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
		return node.isEditable() && node.getUnits()!=null;
	}

	@Override
	protected Object getValue(Object element) {
		if (!(element instanceof NumericNode)) return null;
		
		NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
		
		return node.getUnitIndex();
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (!(element instanceof NumericNode)) return;
		
		NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
		node.setUnitIndex((Integer)value);
		viewer.refresh(element);
	}

	
}