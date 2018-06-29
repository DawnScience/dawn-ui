package org.dawnsci.dedi.configuration.calculations.scattering;

import javax.measure.Quantity;

public abstract class BeamQuantity<T extends Quantity<T>> {
	Quantity<T> amount;

	public enum BeamQuantities {WAVELENGTH, ENERGY}

	public BeamQuantity() {
	}

	public BeamQuantity(Quantity<T> amount) {
		super();
		this.amount = amount;
	}

	public <U extends Quantity<U>, E extends BeamQuantity<U>> E to(E quantity) {
		quantity.setValue(this.toWavelength());
		return quantity;
	}

	public abstract Wavelength toWavelength();

	public abstract void setValue(Wavelength wavelength);

	public Quantity<T> getValue() {
		return amount;
	}

	@SuppressWarnings("unchecked")
	public void setValue(Quantity<?> amount) {
		this.amount = (Quantity<T>) amount;
	}
}