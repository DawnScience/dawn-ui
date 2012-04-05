package org.dawnsci.rcp.functions.classes;

public class SqrtTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		return Math.sqrt(value);
	}

}
