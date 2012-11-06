/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dawnsci.common.widgets.celleditor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Spinner;

/**
 * A cell editor that presents a list of items in a spinner box.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SpinnerCellEditor extends CellEditor {
	/**
	 * The custom combo box control.
	 */
	protected Spinner spinner;

	private FocusAdapter focusListener;

	private KeyListener keyListener;

	/**
	 * Default ComboBoxCellEditor style
	 */
	private static final int defaultStyle = SWT.NONE;

	/**
	 * Creates a new cell editor with no control and no st of choices.
	 * Initially, the cell editor has no cell validator.
	 *
	 * @since 2.1
	 * @see CellEditor#setStyle
	 * @see CellEditor#create
	 * @see ComboBoxCellEditor#setItems
	 * @see CellEditor#dispose
	 */
	public SpinnerCellEditor() {
		setStyle(defaultStyle);
	}

	/**
	 * Spinner Editor
	 *
	 * @param parent
	 *            the parent control
	 */
	public SpinnerCellEditor(Composite parent) {
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
	public SpinnerCellEditor(Composite parent,int style) {
		super(parent, style);
	}

	@Override
	protected Control createControl(Composite parent) {

		spinner = new Spinner(parent, getStyle());
		spinner.setFont(parent.getFont());
		this.focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				SpinnerCellEditor.this.focusLost();
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
					SpinnerCellEditor.this.focusLost();
				}
				if (e.character == '\r') {
					SpinnerCellEditor.this.focusLost();
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
		return spinner.getSelection();
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
		Assert.isTrue(spinner != null && (value instanceof Integer));
		spinner.setSelection(((Integer)value).intValue());
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
	public void setMaximum(int i) {
		if (spinner!=null) spinner.setMaximum(i);
	}
	/**
	 * @param i
	 */
	public void setMinimum(int i) {
		if (spinner!=null) spinner.setMinimum(i);
	}

	public Spinner getSpinner() {
		return spinner;
	}
	
	protected int getDoubleClickTimeout() {
		return 0;
	}
}
