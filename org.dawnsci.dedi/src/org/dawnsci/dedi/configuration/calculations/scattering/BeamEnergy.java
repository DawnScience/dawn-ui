package org.dawnsci.dedi.configuration.calculations.scattering;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;

public class BeamEnergy extends BeamQuantity<Energy> {

	public BeamEnergy() {
	}

	public BeamEnergy(Quantity<Energy> value) {
		super(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Wavelength toWavelength() {
		return new Wavelength((Quantity<Length>) getValue().inverse().multiply(UnitUtils.c).multiply(UnitUtils.h));
	}

	@Override
	public void setValue(Wavelength wavelength) {
		setValue(wavelength.getValue().inverse().multiply(UnitUtils.c).multiply(UnitUtils.h));
	}
}