/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.grid;

import java.util.Map;

import javax.measure.Dimension;
import javax.measure.Unit;
import javax.measure.UnitConverter;

import tec.units.indriya.AbstractUnit;
import tec.units.indriya.function.RationalConverter;
import tec.units.indriya.quantity.QuantityDimension;

public class PerMilliMetre extends AbstractUnit<Resolution> {

	private static final long serialVersionUID = 1170501988959781081L;

	public PerMilliMetre() {
		super();
	}

	@Override
	public Map<? extends Unit<?>, Integer> getBaseUnits() {
		return null;
	}

	@Override
	public Dimension getDimension() {
		return QuantityDimension.LENGTH.pow(-1);
	}

	@Override
	public UnitConverter getSystemConverter() {
		return new RationalConverter(1000, 1);
	}

	@Override
	protected Unit<Resolution> toSystemUnit() {
		return Resolution.UNIT;
	}

	@Override
	public boolean equals(Object arg0) {
		return this == arg0;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
