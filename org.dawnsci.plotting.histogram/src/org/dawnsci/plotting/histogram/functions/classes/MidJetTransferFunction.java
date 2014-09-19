/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions.classes;

public class MidJetTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		if (value > 1.0/3.0 && value < 2.0/3.0  ) return 1;
		
		if (value < 1.0/3.0 && value > 1.0/8.0) return 24.0/5*value - 0.6;
		
		if (value > 2.0/3.0 && value < 7.0/8.0) return -24.0/5*value + 4.2;
		
		return 0;
	}

}
