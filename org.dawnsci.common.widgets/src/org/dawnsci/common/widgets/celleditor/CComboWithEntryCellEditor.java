/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.celleditor;

import java.util.Arrays;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

public class CComboWithEntryCellEditor extends CComboCellEditor {

	/**
	 * The currently entered string 
	 */
	String entered_text;


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
	public CComboWithEntryCellEditor() {
		setStyle(defaultStyle);
	}

	/**
	 * Creates a new cell editor with a combo containing the given list of
	 * choices and parented under the given control. The cell editor value is
	 * the currently selected or manually entered text. Initially, the cell editor has
	 * no cell validator and the first item in the list is selected.
	 *
	 * @param parent
	 *            the parent control
	 * @param items
	 *            the list of strings for the combo box
	 */
	public CComboWithEntryCellEditor(Composite parent, String[] items) {
		this(parent, items, defaultStyle);
	}

	/**
	 * Creates a new cell editor with a combo containing the given list of
	 * choices and parented under the given control. The cell editor value is
	 * the currently selected or manually entered text. Initially, the cell editor has
	 * no cell validator and the first item in the list is selected.
	 *
	 * @param parent
	 *            the parent control
	 * @param items
	 *            the list of strings for the combo box
	 * @param style
	 *            the style bits
	 * @since 2.1
	 */
	public CComboWithEntryCellEditor(Composite parent, String[] items, int style) {
		super(parent, items, style);
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	@Override
	protected Control createControl(Composite parent) {

		comboBox = new CCombo(parent, getStyle());
		comboBox.setFont(parent.getFont());
		comboBox.setEditable(true);

		populateComboBoxItems();

		comboBox.addKeyListener(new KeyAdapter() {
			// hook key pressed - see PR 14201
			@Override
			public void keyPressed(KeyEvent e) {
				keyReleaseOccured(e);
			}
		});

		comboBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				applyEditorValueAndDeactivate();
			}

			@Override
			public void widgetSelected(SelectionEvent event) {
				entered_text = comboBox.getText();
				applyEditorValueAndDeactivate();
			}
		});

		comboBox.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE
						|| e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
				}
			}
		});

		comboBox.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				CComboWithEntryCellEditor.this.focusLost();
			}
		});
		return comboBox;
	}
	
	
	
	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method returns the currently
	 * selected or entered text
	 *
	 * @return The currently entered or selected text as a 
	 *         <code>String</code>
	 */
	@Override
	protected Object doGetValue() {
		if (entered_text == null)
			return new String("");
		return new String(entered_text);
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method accepts a String 
	 *
	 * @param value
	 *            the newly entered text as
	 *            <code>String</code>
	 */
	@Override
	protected void doSetValue(Object value) {
		if (!(value instanceof String)) return;
		//if value is in the list, select it
		comboBox.select(Arrays.asList(getItems()).indexOf(value));
		comboBox.setText((String) value);
	}


	@Override
	public void applyEditorValueAndDeactivate() {
		// must set the selection before getting value
		entered_text = comboBox.getText();
		applyEditorValueAndDeactivate(0);
	}
}
