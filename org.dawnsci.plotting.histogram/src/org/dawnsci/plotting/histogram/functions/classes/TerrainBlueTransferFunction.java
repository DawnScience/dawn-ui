/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions.classes;

public class TerrainBlueTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		double y = 1;
		// 0 <= X <= 24
		if (value <= 0.09411765)
			y = ((4.25 * value) + 0.6);
		// 24 < X < 25
		if (value > 0.09411765 && value <= 0.09803922)
			y = 1;
		// 25 < X < 37
		if (value > 0.09803922 && value <= 0.14509804)
			y = 2.29164441 - (13.17477302 * value);
		// 37 < X <38
		if (value > 0.14509804 && value <= 0.14901961)
			y = 0.38001068;
		// 38 < X < 101
		if (value > 0.14901961 && value <= 0.39607843)
			y = ((0.890466926 * value) + 0.24728770885);
		// 101 < X <102
		if (value > 0.39607843 && value <= 0.4)
			y = 0.59998474;
		// 102 < X < 152
		if (value > 0.4 && value <= 0.59607843)
			y = ((-1.4789105058366 * value) + 1.1915489433127);
		// 152 < X <153
		if (value > 0.59607843 && value <= 0.6)
			y = 0.30998703;
		// 153 < X < 255
		if (value > 0.6 && value <= 1)
			y = ((1.72499 * value) - 0.72501);
		y = y > 1 ? 1 : y;
		y = y < 0 ? 0 : y;
		return y;
	}


}
