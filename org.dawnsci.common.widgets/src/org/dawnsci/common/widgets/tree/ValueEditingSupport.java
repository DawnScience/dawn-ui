/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import javax.measure.quantity.Quantity;

import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
import org.dawnsci.common.widgets.celleditor.FloatSpinnerCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
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
			return createNumericEditor((NumericNode<?>) element);
		}
		if (element instanceof ColorNode) {
			return createColorEditor((ColorNode) element);
		}
		if (element instanceof ComboNode) {
			return createComboEditor((ComboNode) element);
		}
		if (element instanceof LabelNode) {
			return new TextCellEditor((Composite)viewer.getControl(), SWT.NONE);
		}
		return null;
	}

	protected CellEditor createColorEditor(final ColorNode element) {
		final ColorCellEditor ce = new ColorCellEditor((Composite) viewer.getControl(), SWT.NONE);
		return ce;
	}

	private boolean somethingChanged = false;
	
	protected CellEditor createNumericEditor(final NumericNode<?> element) {
		
		final NumericNode<? extends Quantity> node = element;
		
		somethingChanged = false;
		
		final FloatSpinnerCellEditor fse = new NumericNodeEditor(element, (Composite)viewer.getControl(), SWT.NONE);
		fse.setFormat(7, getDecimalPlaces(node));
		fse.setIncrement(node.getIncrement());
		fse.setMaximum(node.getUpperBoundDouble());
		fse.setMinimum(node.getLowerBoundDouble());
		fse.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				somethingChanged = true;
				if (e.character=='\n') {
					setValue(element, fse.getValue());
				}
			}
		});
		fse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				somethingChanged = true;
				node.setValue((Double)fse.getValue(), null);
			}
		});
		return fse;
	}
	

	/**
	 * Gets the decimal places used to view the number
	 * @return
	 */
	private int getDecimalPlaces(NumericNode<? extends Quantity> node) {
		final String formatString = node.getFormat();
		try {
			if (formatString!=null && formatString.indexOf('.')>-1) {
				return formatString.split("\\.")[1].length()+1;
			}
		} catch (Exception ignored) {
			// Just use getMaximumFractionDigits();
		}
		return 4;
	}

	
	private class NumericNodeEditor extends FloatSpinnerCellEditor {
		
		private NumericNode<?> element;
		public NumericNodeEditor(NumericNode<?> element, Composite control, int switches) {
			super(control, switches);
			this.element = element;
		}
		@Override
		protected Object doGetValue() {
			if (somethingChanged) {
			    return super.doGetValue();
			} else {
				return element.getDoubleValue();
			}
		}
		@Override
		protected void doSetValue(Object value) {
			super.doSetValue(value);
		}
		@Override
		public void dispose() {
			super.dispose();
			somethingChanged = false;
		}
	}
	
	protected CellEditor createComboEditor(final ComboNode element) {
		final CComboCellEditor cce = new CComboCellEditor((Composite)viewer.getControl(), element.getStringValues(), SWT.READ_ONLY);
		cce.getCombo().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setValue(element, cce.getValue());
			}
		});
		return cce;
	}

	@Override
	protected boolean canEdit(Object element) {
		if (!(element instanceof LabelNode)) return false;
		return ((LabelNode)element).isEditable();
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof NumericNode) {
			NumericNode<? extends Quantity> node = (NumericNode<?>)element;
			return node.getDoubleValue();
		}
		if (element instanceof ColorNode) {
			ColorNode node = (ColorNode) element;
			return node.getColor().getRGB();
		}
		if (element instanceof ComboNode) {
			ComboNode node = (ComboNode) element;
			return node.getValue();
		}
		if (element instanceof ObjectNode) {
			ObjectNode node = (ObjectNode) element;
			return node.isEditable() ? node.getValue() : null;
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof NumericNode) {
			NumericNode<? extends Quantity> node = (NumericNode<?>) element;
			node.setDoubleValue((Double) value);
		}

		if (element instanceof ColorNode) {
			ColorNode node = (ColorNode) element;
			if (value instanceof RGB) {
				node.setColor(new Color(null, (RGB) value));
			} else {
				node.setColor((Color) value);
			}
			viewer.setSelection(null);
		}
		
		if (element instanceof ComboNode) {
			ComboNode node = (ComboNode)element;
			node.setValue((Integer) value);
		}
		
		if (element instanceof ObjectNode) {
			ObjectNode node = (ObjectNode)element;
			node.setValue((String) value);
		}

		viewer.refresh(element);
	}
}
