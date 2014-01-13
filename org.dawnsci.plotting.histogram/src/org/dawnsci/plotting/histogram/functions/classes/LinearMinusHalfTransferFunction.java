package org.dawnsci.plotting.histogram.functions.classes;


public class LinearMinusHalfTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		return Math.abs((value-0.5));
	}


}
