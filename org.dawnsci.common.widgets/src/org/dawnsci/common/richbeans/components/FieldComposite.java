/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components;

import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.event.BoundsListener;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.event.ValueListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 *
 */
public abstract class FieldComposite extends Composite implements IFieldWidget {

	/**
	 * Can be null!
	 */
	protected Control              mainControl;
	
	/**
	 * Events managed here.
	 */
	protected EventManagerDelegate eventDelegate;

	/**
	 *
	 */
	public enum NOTIFY_TYPE {
	/**
	 * Default
	 */
	DEFAULT, 
	/**
	 * Whenever new value received even if from internal event. Does not
	 * ignore the off flag though.
	 */
	VALUE_CHANGED,
	
	/**
	 * Will tell listeners that value changed even if off.
	 */
	ALWAYS}

	protected NOTIFY_TYPE notifyType;
	
	/**
	 * @param type
	 */
	public void setNotifyType(NOTIFY_TYPE type) {
		this.notifyType = type;
	}
	
	/**
	 * @param parent
	 * @param style
	 */
	public FieldComposite(Composite parent, int style) {
		super(parent, style);
		this.eventDelegate = new EventManagerDelegate(this);
	}

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
	

	/** DO not set this, use off() and on() **/
	private volatile boolean on = false;
	
	/**
	 * @return the on
	 */
	@Override
	public boolean isOn() {
		return on;
	}

	/**
	 * off listeners, and notifications.
	 */
	@Override
	public void off() {	
		this.on = false;
	}
	/**
	 * on listeners, and notifications.
	 */
	@Override
	public void on() {
		this.on = true;
	}

	/**
	 * @return Returns the notifyType.
	 */
	public NOTIFY_TYPE getNotifyType() {
		return notifyType;
	}
	
	@Override
	public void setEnabled(final boolean isEnabled) {
		if (mainControl!=null) mainControl.setEnabled(isEnabled);
	}

	@Override
	public void addValueListener(ValueListener l) {
		eventDelegate.addValueListener(l);
	}
	
	@Override
	public void fireValueListeners() {
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		eventDelegate.notifyValueListeners(evt);
	}

	@Override
	public void fireBoundsUpdaters() {
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		eventDelegate.notifyBoundsProviderListeners(evt);
	}
	
	@Override
	public void dispose() {
		if (eventDelegate!=null) eventDelegate.dispose();
		if (mainControl!=null)   mainControl.dispose();
		super.dispose();
	}

	/**
	 * Remove a certain listener - can also use the clearListeners(...) method.
	 * @param l
	 */
	@Override
	public void removeValueListener(final ValueListener l) {
		eventDelegate.removeValueListener(l);
	}
	
	/**
	 * Add a listener to be notified of the user entering new values
	 * into the widget.
	 * @param l
	 */
	public void addBoundsListener(final BoundsListener l) {
		eventDelegate.addBoundsListener(l);
	}
	
	/**
	 * Remove a certain listener - can also use the clearListeners(...) method.
	 * @param l
	 */
	public void removeBoundsListener(final BoundsListener l) {
		eventDelegate.removeBoundsListener(l);
	}

	// Important to start with true!
	protected boolean active = true;

	@Override
	public boolean isActivated() {
		return active;
	}
	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * @return string including fieldName
	 * 
	 */
	@Override
	public String toString() {
		return fieldName+" ["+getClass().getName()+"]";
	}

	/**
	 *
	 * @return the underlying control used in the widget for entering data.
	 *         NOTE this can be null depending on implementation.
	 */
	public Control getControl() {
		return mainControl;
	}

	/**
	 * If the widget opens a dialog this method should be implemented to close it.
	 */
	public void closeDialog() {
		
	}
}
