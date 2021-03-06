package org.dawnsci.plotting.histogram.functions.classes;

import org.dawnsci.plotting.histogram.data.HistogramData;
import org.dawnsci.plotting.histogram.data.HistogramData.RGBChannel;

public class ViridisRedTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		return HistogramData.getPointFromRGBData(value, HistogramData.VIRIDIS, RGBChannel.RED);
	}

}
