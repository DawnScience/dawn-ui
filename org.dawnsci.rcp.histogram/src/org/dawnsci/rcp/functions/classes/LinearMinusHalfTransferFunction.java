package org.dawnsci.rcp.functions.classes;


public class LinearMinusHalfTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		return Math.abs((value-0.5));
	}


}
