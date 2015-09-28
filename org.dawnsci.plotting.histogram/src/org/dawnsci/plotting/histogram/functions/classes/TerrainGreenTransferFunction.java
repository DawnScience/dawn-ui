/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions.classes;

public class TerrainGreenTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		double y = 1;
		// 0 <= X <= 37
		if (value <= 0.14509804)
			y = ((4.13514 * value) + 0.2);
		// 37 < X < 38
		if (value > 0.14509804 && value <= 0.14901961)
			y = 0.8;
		// 38 < X < 101
		if (value > 0.14901961 && value <= 0.39607843)
			y = ((0.80952381 * value) + 0.67936508);
		// 101 < X <102
		if (value > 0.39607843 && value <= 0.4)
			y = 1;
		// 102 < X < 152
		if (value > 0.4 && value <= 0.59607843)
			y = ((-3.26396887 * value) + 2.30558755);
		// 152 < X <153
		if (value > 0.59607843 && value <= 0.6)
			y = 0.36000610;
		// 153 < X < 255
		if (value > 0.6 && value <= 1)
			y = ((1.59994660 * value) - 0.59996185);
		y = y > 1 ? 1 : y;
		y = y < 0 ? 0 : y;
		return y;
	}
}
