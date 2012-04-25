package org.dawnsci.rcp.functions.classes;


public class NCDGreenTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		if (value >= 0.749) return 1;
		if (value <= 0.447) return 0;
		if (value <= 0.569) return 0.639 * (value - 0.447) / (0.569 - 0.447);
		if (value >= 0.690) return 0.639 + (1 - 0.639) * (value - 0.690) / (0.749 - 0.690); 
		return 0.639;
	}


}
