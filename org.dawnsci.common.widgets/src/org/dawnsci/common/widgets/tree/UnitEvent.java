/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import java.util.EventObject;

import javax.measure.Quantity;
import javax.measure.Unit;

public class UnitEvent<Q extends Quantity<?>> extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3954307816969836173L;
	private Unit<?> unit;

	public UnitEvent(Object source, Unit<?> unit) {
		super(source);
		this.unit = unit;
		
	}

	public Unit<?> getUnit() {
		return unit;
	}

	public void setUnit(Unit<?> unit) {
		this.unit = unit;
	}
}
