/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.wrappers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.components.ButtonComposite;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Class designed to wrap combo and allow BeanUI to talk to the class
 * to build up XML files from the values and link them back to the bean.
 * 
 * You have to be a widget (even though not needed) so that RCP developer
 * can deal with using the class. Therefore in inherits from Composite.
 */
public class ComboWrapper extends ButtonComposite implements IFieldWidget{

	
	protected final Combo combo;
	protected AutoCompleteField autoComplete;
	protected Map<String, ?> itemMap;
	private SelectionAdapter selectionListener;
	private ModifyListener modifyListener;
	
	/**
	 * Construct combo.
	 * @param parent
	 * @param style
	 */
	public ComboWrapper(final Composite parent, int style) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));
		
		final int textStyle = style==SWT.NONE?SWT.BORDER:style;
		combo = new Combo(this, textStyle);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		mainControl = combo;
		
		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ComboWrapper.this.isOn()){
					fireValueListeners();
				}
			}
		};
		combo.addSelectionListener(selectionListener);
		
		this.modifyListener = new ModifyListener() {
			private List<String> items;
			@Override
			public void modifyText(ModifyEvent e) {
				final String txt = combo.getText();
				if (items==null) items = Arrays.asList(combo.getItems());
				if (items.contains(txt)) {
					if (ComboWrapper.this.isOn()){
						fireValueListeners();
					}
				}						
			}
		};
		combo.addModifyListener(modifyListener);
	
	}
	
	/**
	 * @param fieldName
	 */
	@Override
	public void setFieldName(String fieldName) {
		super.setFieldName(fieldName);
		if (combo!=null&&!combo.isDisposed()) combo.setToolTipText(fieldName);
	}
	
	@Override
	public void dispose() {
		if (combo!=null&&!combo.isDisposed()) {
			combo.removeSelectionListener(selectionListener);
			combo.removeModifyListener(modifyListener);
		}
		super.dispose();
	}
	
	/**
	 * Can be called to update things from linkUI();
	 */
	@Override
	public void fireValueListeners() {
		final ValueEvent evt = new ValueEvent(combo,getFieldName());
		evt.setDoubleValue(combo.getSelectionIndex());
		evt.setValue(getValue());
		eventDelegate.notifyValueListeners(evt);
	}

	/**
	 * If the visible items and the value in the data are not equal.
	 * @param itemMap
	 */
	public void setItems(final Map<String,?> itemMap) {
		setItems(itemMap.keySet().toArray(new String [itemMap.size()]));
		this.itemMap = itemMap;
	}
	
	/**
	 * @param items
	 */
	public void setItems(final String [] items) {
		combo.setItems(items);
		// Auto-complete in Jface not working well.
		if (autoComplete==null) autoComplete = new AutoCompleteField(combo, new ComboContentAdapter(), items) ;
		autoComplete.setProposals(items);
	}

	@Override
	public Object getValue() {
		if (combo == null)
			return null;
		if (combo.isDisposed())
			return null;
		final String text = combo.getText();
		final int index = combo.getSelectionIndex();
		if (index < 0) {
			return itemMap != null ? itemMap.get(text) : text;
		} else if (index > -1) {
			return itemMap != null ? itemMap.get(combo.getItem(index)) : combo.getItem(index);
		}
		return null;
	}

	@Override
	public void setValue(Object value) {
		if (value == null) {
			combo.clearSelection();
			if (combo.getSelectionIndex() != -1) {
				throw new IllegalArgumentException("Attempted to clearSelection of Read Only ComboWrapper "
						+ getFieldName());
			}
			return;
		}

		String textValue = itemMap != null ? getKeyForValue(value) : value.toString();
		if (textValue == null && itemMap != null && !itemMap.isEmpty())
			textValue = itemMap.keySet().iterator().next();
		if (textValue != null) {
			final int index = combo.indexOf(textValue);
			if (index < 0) {
				combo.clearSelection();
//				throw new IllegalArgumentException("String '" + textValue + "' was not found for ComboBox");
			} else {
				combo.select(index);
			}
		}
	}

	private String getKeyForValue(final Object value) {
		if (itemMap==null) return null;
		if (value  ==null) return null;
		for (Entry<String,?> entry : itemMap.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/*******************************************************************/
	/**        This section will be the same for many wrappers.       **/
	/*******************************************************************/

	/**
	 * @param active the active to set
	 */
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		combo.setVisible(active);
	}

	/**
	 * @param i
	 */
	public void setTextLimit(int i) {
		combo.setTextLimit(i);
	}

	/**
	 * @param indexOf
	 */
	public void select(int indexOf) {
		combo.select(indexOf);
	}

	/**
	 * @param systemColor
	 */
	@Override
	public void setForeground(Color systemColor) {
		if (systemColor.isDisposed()) return;
		combo.setForeground(systemColor);
	}

	/**
	 * @return i
	 */
	public int getSelectionIndex() {
		return combo.getSelectionIndex();
	}

	/**
	 * @param index
	 * @return i
	 */
	public String getItem(int index) {
		if (itemMap!=null) throw new RuntimeException("Cannot use getItem() when values are mapped!");
		return combo.getItem(index);
	}

	/**
	 * 
	 * @return items
	 */
	public String[] getItems() {
		if (itemMap!=null) throw new RuntimeException("Cannot use getItems() when values are mapped!");
		return combo.getItems();
	}

	/**
	 * Creates a map to choose integers while viewing strings
	 * @param items
	 * @return maps
	 */
	public static Map<String, Integer> getItemMap(String[] items) {
		final Map<String,Integer> ret = new LinkedHashMap<String,Integer>(items.length);
		for (int i = 0; i < items.length; i++) {
			ret.put(items[i], i);
		}
		return ret;
	}

	public void demandStep(){
		combo.setEnabled(false);
	}

	public void demandComplete(String position) {
		off();
		setValue(position);
		combo.setEnabled(true);
		on();
	}
}

	
