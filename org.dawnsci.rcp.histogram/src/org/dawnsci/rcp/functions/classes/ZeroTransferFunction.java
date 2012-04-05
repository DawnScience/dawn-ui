package org.dawnsci.rcp.functions.classes;


public class ZeroTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		return 0.0;
	}


}
