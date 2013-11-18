/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import java.util.EventObject;

public class ValueEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9037300720398962478L;
	private Object value;

	public ValueEvent(Object source, Object value) {
		super(source);
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
