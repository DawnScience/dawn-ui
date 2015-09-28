/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions.classes;

public class TerrainRedTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		double y = 1;
		// 0 <= X <= 24
		if (value <= 0.09411765)
			y = ((-2.125 * value) + 0.2);
		// 24 < X < 38
		if (value > 0.09411765 && value <= 0.14901961)
			y = 0;
		// 38 < X < 101
		if (value > 0.14901961 && value <= 0.39607843)
			y = ((4.04761905 * value) - 0.60317460);
		// 101 < X <102
		if (value > 0.396078431 && value <= 0.4)
			y = 1;
		// 102 < X < 152
		if (value > 0.4 && value <= 0.59607843)
			y = ((-2.34599222 * value) + 1.93839689);
		// 152 < X <153
		if (value > 0.59607843 && value <= 0.6)
			y = 0.54000152;
		// 153 < X < 255
		if (value > 0.6 && value <= 1)
			y = ((1.14995804 * value) - 0.14997330);
		y = y > 1 ? 1 : y;
		y = y < 0 ? 0 : y;
		return y;
	}
}
