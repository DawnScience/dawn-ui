/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.grid;

import javax.measure.Quantity;

import tec.units.ri.unit.ProductUnit;
import tec.units.ri.unit.Units;

public interface Resolution<Q extends Quantity<Q>> extends Quantity<Q> {
	public static final ProductUnit<Resolution> UNIT
		= new ProductUnit<Resolution>(Units.METRE.pow(-1));
}
