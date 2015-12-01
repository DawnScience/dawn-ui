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

public class NipyGreenTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		for (int i = 0; i < HistogramData.NIPY_SPECTRAL.length; i++) {
			if (i > 0 && value <= HistogramData.NIPY_SPECTRAL[i][0]) {
				return HistogramData.interpolatedY(
						new double[] { HistogramData.NIPY_SPECTRAL[i - 1][0], HistogramData.NIPY_SPECTRAL[i - 1][2] },
						new double[] { HistogramData.NIPY_SPECTRAL[i][0], HistogramData.NIPY_SPECTRAL[i][2] }, value);
			}
		}
		return 0;
	}
}
