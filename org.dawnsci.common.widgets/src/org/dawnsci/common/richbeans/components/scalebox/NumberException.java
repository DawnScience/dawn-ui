/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.scalebox;

public class NumberException extends Exception {

	private Double value;

	public NumberException(Double dbl) {
		this.value = dbl;
	}

	/**
	 * @return Returns the value.
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(Double value) {
		this.value = value;
	}

}
