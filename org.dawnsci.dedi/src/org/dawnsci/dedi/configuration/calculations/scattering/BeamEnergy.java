package org.dawnsci.dedi.configuration.calculations.scattering;

import javax.measure.quantity.Energy;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.Constants;


public class BeamEnergy extends BeamQuantity<Energy> {

	public BeamEnergy() {
	}
	
	public BeamEnergy(Amount<Energy> value) {
		super(value);
	}
	
	@Override
	public Wavelength toWavelength() {
		return new Wavelength(this.getValue().inverse().times(Constants.c).times(Constants.ℎ).to(SI.METER));
	}

	@Override
	public void setValue(Wavelength wavelength) {
		setValue(wavelength.getValue().inverse().times(Constants.c).times(Constants.ℎ).to(Energy.UNIT));
	}
}