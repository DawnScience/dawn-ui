/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import java.util.EventObject;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

public class UnitEvent<E extends Quantity> extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3954307816969836173L;
	private Unit<E> unit;

	public UnitEvent(Object source, Unit<E> unit) {
		super(source);
		this.unit = unit;
		
	}

	public Unit<E> getUnit() {
		return unit;
	}

	public void setUnit(Unit<E> unit) {
		this.unit = unit;
	}
}
