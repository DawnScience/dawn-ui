/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions.classes;

import org.dawnsci.plotting.histogram.data.HistogramData;

public class EarthRedTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		for (int i = 0; i < HistogramData.EARTH_RED.length; i++) {
			if (i > 0 && value <= HistogramData.EARTH_RED[i][0]) {
				return HistogramData.interpolatedY(
						new double[] { HistogramData.EARTH_RED[i - 1][0], HistogramData.EARTH_RED[i - 1][1] },
						new double[] { HistogramData.EARTH_RED[i][0], HistogramData.EARTH_RED[i][1] }, value);
			}
		}
		return 0;
	}
}
