/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions.classes;


public class NCDBlueTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		if (value >= 0.690) return (value-0.690) / (1-0.690);
		if (value <= 0.192) return value/0.192;
		if (value <= 0.373) return 1 - (value - 0.192) / (0.373 - 0.192);
		if (value <= 0.506) return 0;
		if (value >= 0.624) return 0;
		if (value <= 0.569) return ((value-0.506)/(0.569-0.506)) * 0.322;
		if (value >= 0.569) return (1 - ((value-0.569)/(0.624-0.569))) * 0.322;
		return 0;
	}


}
