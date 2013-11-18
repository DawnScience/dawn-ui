/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.decorator;

import java.util.EventObject;

public class ValueChangeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8983136524290779712L;
	private Number value;

	public ValueChangeEvent(Object source, Number value) {
		super(source);
		this.value = value;
	}

	public Number getValue() {
		return value;
	}

}
