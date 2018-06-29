package org.dawnsci.dedi.configuration.calculations.scattering;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

public class Wavelength extends BeamQuantity<Length> {

	public Wavelength() {
	}

	public Wavelength(Quantity<Length> value) {
		super(value);
	}

	@Override
	public Wavelength toWavelength() {
		return this;
	}

	@Override
	public void setValue(Wavelength wavelength) {
		setValue(wavelength.getValue());
	}
}