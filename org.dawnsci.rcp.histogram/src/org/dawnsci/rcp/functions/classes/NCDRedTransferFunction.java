package org.dawnsci.rcp.functions.classes;


public class NCDRedTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		if (value >= 0.753) return 1;
		if (value <= 0.188) return 0;
		if (value <= 0.251) return 0.316 * (value - 0.188) / (0.251 - 0.188);
		if (value <= 0.306) return 0.316;
		if (value <= 0.431) return 0.316 + (1 - 0.319) * (value - 0.306) / (0.431 - 0.306);
		if (value >= 0.682) return 0.639 + (1 - 0.639) * (value - 0.682) / (0.753 - 0.682);
		if (value >= 0.635) return 1 - (1 - 0.639) * (value - 0.635) / (0.682 - 0.635);
		return 1;	
	}


}
