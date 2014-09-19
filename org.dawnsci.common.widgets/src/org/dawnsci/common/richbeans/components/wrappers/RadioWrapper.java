/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.wrappers;

import java.util.HashMap;
import java.util.Map;

import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.components.EventManagerDelegate;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.event.ValueListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Designed to wrap Text objects to allow then to work with BeanUI
 * @author fcp94556
 *
 */
public class RadioWrapper extends Group implements IFieldWidget {
	
	protected final EventManagerDelegate eventDelegate;
	protected final Map<String,Button>   buttonMap;
	protected final Map<String, SelectionListener> listenerMap;
	
	/**
	 * Simply calls super and adds some listeners.
	 * @param parent
	 * @param style
	 * @param items 
	 */
	public RadioWrapper(Composite parent, int style, String[] items) {

		super(parent, SWT.NONE);	
		setLayout(new RowLayout());
		
		this.eventDelegate = new EventManagerDelegate(this);
		this.buttonMap     = new HashMap<String,Button>(7);
		this.listenerMap   = new HashMap<String,SelectionListener>(7);
		
		for (int i = 0; i < items.length; i++) {
			final Button button = new Button(this, SWT.RADIO | style);
			button.setText(items[i]);
			buttonMap.put(items[i],button);
			final SelectionListener selectionListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final ValueEvent evt = new ValueEvent(button,getFieldName());
					evt.setValue(getValue());
					eventDelegate.notifyValueListeners(evt);
				}
			};
			listenerMap.put(items[i],selectionListener);
			button.addSelectionListener(selectionListener);
		}
	}
	
	@Override
	public void dispose() {
		for (String key : buttonMap.keySet()) {
			Button button = buttonMap.get(key);
			if (!button.isDisposed())
				button.removeSelectionListener(listenerMap.get(key));
		}
		super.dispose();
	}

	
	@Override
	public Object getValue() {
		for (String  label : buttonMap.keySet()) {
			if (buttonMap.get(label).getSelection()) return label;
		}
		return null;
	}
	
	@Override
	public void setValue(Object value) {
		for (String  label : buttonMap.keySet()) {
			if (label.equals(value)) {
				buttonMap.get(label).setSelection(true);
			} else {
				buttonMap.get(label).setSelection(false);
			}
		}
	}

	/**
	 * Clears all buttons of value.
	 */
	public void clear() {
		for (String  label : buttonMap.keySet()) {
			buttonMap.get(label).setSelection(false);
		}
	}
	/*******************************************************************/
	/**        This section will be the same for many wrappers.       **/
	/*******************************************************************/
	@Override
	protected void checkSubclass () {
	}
	private boolean isOn = false;
	@Override
	public boolean isOn() {return isOn;}
	@Override
	public void off() {isOn = false;}
	@Override
	public void on() {isOn = true;}
	
	// Important to start with true!
	private boolean active = true;

	@Override
	public boolean isActivated() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
		setVisible(active);
	}
	
	@Override
	public void addValueListener(ValueListener l) {
		eventDelegate.addValueListener(l);
	}

	/*******************************************************************/
	protected String fieldName;
	/**
	 * @return b
	 */
	@Override
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName
	 */
	@Override
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	@Override
	public void fireValueListeners() {
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		eventDelegate.notifyValueListeners(evt);
	}
	/**
	 * 
	 */
	@Override
	public void fireBoundsUpdaters() {
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		eventDelegate.notifyBoundsProviderListeners(evt);
	}

	@Override
	public void removeValueListener(ValueListener listener) {
		eventDelegate.removeValueListener(listener);
	}

}

	