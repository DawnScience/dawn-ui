/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.event;

import java.util.EventObject;

import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;


/**
 * Custom event object used in notification of custom widgets
 * 
 * @see ScaleBox
 * @author fcp94556
 *
 */
public class ValueEvent extends EventObject {
	
	private static final long serialVersionUID = -2193419622660949003L;
	private double doubleValue;
	private Object value;
	private String fieldName;

	public ValueEvent(final Object source, final String field) {
		super(source);
		this.fieldName = field;
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
}

	