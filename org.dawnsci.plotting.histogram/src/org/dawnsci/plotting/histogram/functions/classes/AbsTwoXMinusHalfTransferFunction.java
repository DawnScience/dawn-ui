/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions.classes;


public class AbsTwoXMinusHalfTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		double result = Math.abs((2.0*value)-0.5);
		if (result < 0) return 0.0;
		if (result > 1.0) return 1.0;
		return result;
	}


}
