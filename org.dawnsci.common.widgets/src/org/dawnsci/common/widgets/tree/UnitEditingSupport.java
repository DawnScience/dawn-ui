/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import javax.measure.Quantity;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.richbeans.widgets.cell.CComboCellEditor;
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
			@SuppressWarnings("unchecked")
			NumericNode<? extends Quantity<?>> node = (NumericNode<? extends Quantity<?>>)element;
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

		@SuppressWarnings("unchecked")
		NumericNode<? extends Quantity<?>> node = (NumericNode<? extends Quantity<?>>)element;
		return node.isEditable() && node.getUnits()!=null;
	}

	@Override
	protected Object getValue(Object element) {
		if (!(element instanceof NumericNode)) return null;

		@SuppressWarnings("unchecked")
		NumericNode<? extends Quantity<?>> node = (NumericNode<? extends Quantity<?>>)element;
		
		return node.getUnitIndex();
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (!(element instanceof NumericNode)) return;

		@SuppressWarnings("unchecked")
		NumericNode<? extends Quantity<?>> node = (NumericNode<? extends Quantity<?>>)element;
		node.setUnitIndex((Integer)value);
		viewer.refresh(element);
	}
}