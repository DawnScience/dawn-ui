package org.dawnsci.plotting.tools.grid;

import javax.measure.quantity.Quantity;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;

public interface Resolution extends Quantity {
	public static final ProductUnit<Resolution> UNIT
		= new ProductUnit<Resolution>(SI.METRE.pow(-1));
}
