package org.dawnsci.plotting.histogram.functions.classes;

public class SqrtTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		return Math.sqrt(value);
	}

}
