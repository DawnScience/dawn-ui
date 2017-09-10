package org.dawnsci.dedi.configuration.calculations.scattering;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;


public class Wavelength extends BeamQuantity<Length> {

	public Wavelength() {
	}
	
	public Wavelength(Amount<Length> value) {
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