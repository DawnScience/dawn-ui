package org.dawnsci.common.widgets.tree;

import javax.measure.quantity.Quantity;

import org.dawnsci.common.widgets.celleditor.FloatSpinnerCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

public class ValueEditingSupport extends EditingSupport {

	private ColumnViewer viewer;

	public ValueEditingSupport(ColumnViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		
		if (element instanceof NumericNode) {
			return createNumericEditor(element);
		}
		if (element instanceof ColorNode) {
			return createColorEditor(element);
		}
		return null;
	}
	
	private CellEditor createColorEditor(final Object element) {

		final ColorCellEditor ce = new ColorCellEditor((Composite)viewer.getControl(), SWT.NONE);
		return ce;
	}

	private CellEditor createNumericEditor(final Object element) {
		
		final NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
		final FloatSpinnerCellEditor fse = new FloatSpinnerCellEditor((Composite)viewer.getControl(), SWT.NONE);
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

	@Override
	protected boolean canEdit(Object element) {
		if (!(element instanceof LabelNode)) return false;
		return ((LabelNode)element).isEditable();
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof NumericNode) {
			NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			return node.getDoubleValue();
		}
		if (element instanceof ColorNode) {
			ColorNode node = (ColorNode)element;
			return node.getColor().getRGB();
		}
		
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof NumericNode)  {
		
			NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			node.setDoubleValue((Double)value);
		} 
		
		if (element instanceof ColorNode) {
			ColorNode node = (ColorNode)element;
			if (value instanceof RGB) {
			    node.setColor(new Color(null, (RGB)value));
			} else {
				node.setColor((Color)value);
			}
			viewer.setSelection(null);
		}
		
		viewer.refresh(element);
	}

	
}
