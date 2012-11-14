package org.dawnsci.rcp.functions.classes;

public class MidJetTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		if (value > 1.0/3.0 && value < 2.0/3.0  ) return 1;
		
		if (value < 1.0/3.0 && value > 1.0/8.0) return 24.0/5*value - 0.6;
		
		if (value > 2.0/3.0 && value < 7.0/8.0) return -24.0/5*value + 4.2;
		
		return 0;
	}

}
