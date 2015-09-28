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
			y = ((-2.5094805756284 * value) + 1.3739618112701);
		// 37 < X <38
		if (value > 0.14509804 && value <= 0.14901961)
			y = 0.38001068;
		// 38 < X < 101
		if (value > 0.14901961 && value <= 0.39607843)
			y = ((0.49204723871937 * value) + 0.30668599476107);
		// 101 < X <102
		if (value > 0.39607843 && value <= 0.4)
			y = 0.59998474;
		// 102 < X < 152
		if (value > 0.4 && value <= 0.59607843)
			y = ((-1.4789105058366 * value) + 1.1915489433127);
		// 152 < X <153
		if (value > 0.59607843 && value <= 0.6)
			y = 0.31000229;
		// 153 < X < 255
		if (value > 0.6 && value <= 0.59607843)
			y = ((1.72495613 * value) - 0.72497139);
		y = y > 1 ? 1 : y;
		y = y < 0 ? 0 : y;
		return y;
	}


}
