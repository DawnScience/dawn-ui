package org.dawnsci.common.widgets.tree;

import javax.measure.quantity.Quantity;

import org.dawnsci.common.widgets.celleditor.FloatSpinnerCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ValueEditingSupport extends EditingSupport {

	private TreeViewer viewer;

	public ValueEditingSupport(TreeViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		
		// TODO Colour editor.
		if (element instanceof NumericNode) {
			final NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			final FloatSpinnerCellEditor fse = new FloatSpinnerCellEditor(viewer.getTree(), SWT.NONE);
			fse.setFormat(7, node.getDecimalPlaces()+1);
			fse.setIncrement(node.getIncrement());
			fse.setMaximum(node.getUpperBoundDouble());
			fse.setMinimum(node.getLowerBoundDouble());
			fse.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.character=='\n') {
						setValue(element, fse.getValue());
					}
				}
			});
			fse.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					node.setValue((Double)fse.getValue(), null);
				}
			});
			return fse;
		}
		return null;
	}

	@Override
	protected boolean canEdit(Object element) {
		if (!(element instanceof NumericNode)) return false;
		return ((NumericNode<?>)element).isEditable();
	}

	@Override
	protected Object getValue(Object element) {
		if (!(element instanceof NumericNode)) return null;
		
		NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
		
		return node.getDoubleValue();
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (!(element instanceof NumericNode)) return;
		
		NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
		node.setDoubleValue((Double)value);
		viewer.refresh(element);
	}

	
}
