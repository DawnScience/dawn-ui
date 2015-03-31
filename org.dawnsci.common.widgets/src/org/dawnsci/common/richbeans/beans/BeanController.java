/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.beans;

import java.util.List;

import org.dawnsci.common.richbeans.event.ValueListener;

/**
 * This class acts as a non-static wrapper around BeanUI, removing the necessity
 * to pass the bean and UI objects to all method calls. Once this class has been
 * instantiated with UI and bean objects, all underlying calls to BeanUI should
 * have the arguments in the correct order, making it much simpler to use the
 * BeanUI methods correctly.
 * 
 * @author Colin Palmer
 */
public class BeanController {

	private final Object ui;
	private final Object bean;

	/**
	 * Create a new BeanController with the given UI and bean objects. Note the
	 * correct order of the arguments!
	 */
	public BeanController(Object ui, Object bean) {
		this.ui = ui;
		this.bean = bean;
	}

	public Object getUi() {
		return ui;
	}

	public Object getBean() {
		return bean;
	}

	/**
	 * Send the bean values to the UI
	 * <p>
	 * Code calling this method should normally call switchUIOff() first and
	 * switchUIOn() afterwards to avoid an infinite loop of value changed events
	 */
	public void beanToUI() throws Exception {
		BeanUI.beanToUI(bean, ui);
	}

	/**
	 * Send the UI values to the bean
	 */
	public void uiToBean() throws Exception {
		BeanUI.uiToBean(ui, bean);
	}

	/**
	 * Send the value of a single named field from the UI to the bean
	 */
	public void uiToBean(String fieldName) throws Exception {
		BeanUI.uiToBean(ui, bean, fieldName);
	}

	/**
	 * Recursively switch all UI elements on
	 */
	public void switchUIOn() throws Exception {
		BeanUI.switchState(ui, true);
	}

	/**
	 * Recursively switch all UI elements off
	 */
	public void switchUIOff() throws Exception {
		BeanUI.switchState(ui, false);
	}

	/**
	 * Recursively enable all UI elements
	 */
	public void enableUI() throws Exception {
		setUIEnabled(true);
	}

	/**
	 * Recursively disable all UI elements
	 */
	public void disableUI() throws Exception {
		setUIEnabled(false);
	}
	
	/**
	 * Recursively set the enabled state of all UI elements
	 */
	public void setUIEnabled(boolean isEnabled) throws Exception {
		BeanUI.setEnabled(bean, ui, isEnabled);
	}

	/**
	 * Fire value listeners on all UI elements
	 */
	public void fireValueListeners() throws Exception {
		BeanUI.fireValueListeners(bean, ui);
	}

	/**
	 * Fire bounds updaters on all UI elements
	 */
	public void fireBoundsUpdaters() throws Exception {
		BeanUI.fireBoundsUpdaters(bean, ui);
	}

	/**
	 * Add a value listener to all UI elements
	 */
	public void addValueListener(ValueListener listener) throws Exception {
		BeanUI.addValueListener(bean, ui, listener);
	}

	/**
	 * Remove a value listener from all UI elements
	 */
	public void removeValueListener(ValueListener listener) throws Exception {
		BeanUI.removeValueListener(bean, ui, listener);
	}

	/**
	 * Get the IFieldWidget associated with a single field name
	 */
	public IFieldWidget getFieldWidget(String fieldName) throws Exception {
		return BeanUI.getFieldWiget(fieldName, ui);
	}

	/**
	 * Get a list of all fields which are in the bean and have an associated UI
	 * element
	 */
	public List<String> getEditingFields() throws Exception {
		return BeanUI.getEditingFields(bean, ui);
	}

	// Not sure this is necessary, disposal should probably be handled elsewhere
	public void dispose() throws Exception {
		BeanUI.dispose(bean, ui);
	}
}
