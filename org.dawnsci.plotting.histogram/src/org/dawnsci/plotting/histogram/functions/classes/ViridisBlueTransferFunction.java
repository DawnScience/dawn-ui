package org.dawnsci.plotting.histogram.functions.classes;

import org.dawnsci.plotting.histogram.data.HistogramData;

public class ViridisBlueTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		double div = Double.valueOf(1.0) / 255;
		double val = value / div;
		double rounded = Math.round(val);
		for (int i = 0; i < HistogramData.VIRIDIS.length; i ++) {
			if (i == rounded)
				return HistogramData.VIRIDIS[i][2];
		}
		return 0;
	}

}
