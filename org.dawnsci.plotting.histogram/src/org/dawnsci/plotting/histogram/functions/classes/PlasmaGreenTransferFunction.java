package org.dawnsci.plotting.histogram.functions.classes;

import org.dawnsci.plotting.histogram.data.HistogramData;

public class PlasmaGreenTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		double div = Double.valueOf(1.0) / 255;
		double val = value / div;
		double rounded = Math.round(val);
		for (int i = 0; i < HistogramData.PLASMA.length; i ++) {
			if (i == rounded)
				return HistogramData.PLASMA[i][1];
		}
		return 0;
	}

}
