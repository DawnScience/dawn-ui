/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions.classes;

public class HighJetTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		
		if (value >= 0.9) return -5*value +5.5;
		
		if (value > 0.666 && value <= 0.9 ) return 1;
		
		if (value >=1.0/2.0 && value <= 2.0/3.0 ) return 6*value -3;

		return 0;
	}

}
