/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.celleditor;

import org.dawnsci.common.widgets.spinner.FloatSpinner;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor that presents a list of items in a spinner box.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FloatSpinnerCellEditor extends CellEditor {
	/**
	 * The custom combo box control.
	 */
	protected FloatSpinner spinner;

	private FocusAdapter focusListener;

	private KeyListener keyListener;

	/**
	 * Default ComboBoxCellEditor style
	 */
	private static final int defaultStyle = SWT.NONE;

	/**
	 * Creates a new cell editor with no control and no set of choices.
	 * Initially, the cell editor has no cell validator.
	 *
	 * @since 2.1
	 * @see CellEditor#setStyle
	 * @see CellEditor#create
	 * @see ComboBoxCellEditor#setItems
	 * @see CellEditor#dispose
	 */
	public FloatSpinnerCellEditor() {
		setStyle(defaultStyle);
	}

	/**
	 * Spinner Editor
	 *
	 * @param parent
	 *            the parent control
	 */
	public FloatSpinnerCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	/**
	 * Spinner Editor
	 *
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the style bits
	 * @since 2.1
	 */
	public FloatSpinnerCellEditor(Composite parent,int style) {
		super(parent, style);
	}

	@Override
	protected Control createControl(Composite parent) {

		spinner = new FloatSpinner(parent, getStyle());
		spinner.setFont(parent.getFont());
		this.focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				FloatSpinnerCellEditor.this.focusLost();
			}
		};
		spinner.addFocusListener(focusListener);
		this.keyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == '\n') {
					FloatSpinnerCellEditor.this.focusLost();
				}
				if (e.character == '\r') {
					FloatSpinnerCellEditor.this.focusLost();
				}
			}
		};
		spinner.addKeyListener(keyListener);

		return spinner;
	}
	
	@Override
	public void dispose() {
		if (focusListener!=null) spinner.removeFocusListener(focusListener);
		if (keyListener!=null)   spinner.removeKeyListener(keyListener);
		super.dispose();
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method returns the zero-based index
	 * of the current selection.
	 *
	 * @return the zero-based index of the current selection wrapped as an
	 *         <code>Integer</code>
	 */
	@Override
	protected Object doGetValue() {
		return spinner.getDouble();
	}

	@Override
	protected void doSetFocus() {
		spinner.setFocus();
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method sets the minimum width of the
	 * cell. The minimum width is 10 characters if <code>comboBox</code> is
	 * not <code>null</code> or <code>disposed</code> else it is 60 pixels
	 * to make sure the arrow button and some text is visible. The list of
	 * CCombo will be wide enough to show its longest item.
	 * @return  layoutData
	 */
	@Override
	public LayoutData getLayoutData() {
		LayoutData layoutData = super.getLayoutData();
		if ((spinner == null) || spinner.isDisposed()) {
			layoutData.minimumWidth = 60;
		} else {
			// make the comboBox 10 characters wide
			GC gc = new GC(spinner);
			layoutData.minimumWidth = (gc.getFontMetrics()
					.getAverageCharWidth() * 10) + 10;
			gc.dispose();
		}
		return layoutData;
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method accepts a zero-based index of
	 * a selection.
	 *
	 * @param value
	 *            the zero-based index of the selection wrapped as an
	 *            <code>Integer</code>
	 */
	@Override
	protected void doSetValue(Object value) {
		Assert.isTrue(spinner != null && (value instanceof Number));
		spinner.setDouble(((Number)value).doubleValue());
	}

	/**
	 * Applies the currently selected value and deactivates the cell editor
	 */
	void applyEditorValueAndDeactivate() {
		// must set the selection before getting value
		Object newValue = doGetValue();
		markDirty();
		boolean isValid = isCorrect(newValue);
		setValueValid(isValid);
		fireApplyEditorValue();
		deactivate();
	}

	@Override
	protected void focusLost() {
		if (isActivated()) {
			applyEditorValueAndDeactivate();
		}
	}

	/**
	 * @param i
	 */
	public void setMaximum(double i) {
		if (spinner!=null) spinner.setMaximum(i);
	}
	/**
	 * @param i
	 */
	public void setMinimum(double i) {
		if (spinner!=null) spinner.setMinimum(i);
	}

	public FloatSpinner getSpinner() {
		return spinner;
	}
	protected int getDoubleClickTimeout() {
		return 0;
	}

	public void setIncrement(double inc) {
		if (spinner!=null) spinner.setIncrement(inc);
	}

	/**
	 * Set the format and automatically set minimum and maximum allowed values
	 * 
	 * @param width
	 *            of displayed value as total number of digits
	 * @param precision
	 *            of value in decimal places
	 */
	public void setFormat(int i, int j) {
		if (spinner!=null) spinner.setFormat(i,j);
	}

	public void setBounds(Rectangle bnds) {
		if (spinner!=null) spinner.setBounds(bnds);
	}

	public void addKeyListener(KeyListener listener) {
		if (spinner!=null) spinner.addKeyListener(listener);
	}

	public void addSelectionListener(SelectionListener listener) {
		if (spinner!=null) spinner.addSelectionListener(listener);
	}
}
