package org.dawnsci.dedi.configuration.calculations.scattering;

import javax.measure.quantity.Quantity;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public interface InverseLength extends Quantity {
	public static final Unit<InverseLength> UNIT = new ProductUnit<>(SI.METER.inverse());
}