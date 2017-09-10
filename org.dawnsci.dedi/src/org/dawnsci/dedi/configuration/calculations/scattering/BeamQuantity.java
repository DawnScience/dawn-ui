package org.dawnsci.dedi.configuration.calculations.scattering;


import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;


public abstract class BeamQuantity<T extends Quantity> {
	Amount<T> amount;
    
	public enum Quantities {WAVELENGTH, ENERGY}
	
	
	public BeamQuantity() {
	}
	
	
	public BeamQuantity(Amount<T> amount) {
		super();
		this.amount = amount;
	}

	
	public <U extends Quantity, E extends BeamQuantity<U>> E to(E quantity){
		quantity.setValue(this.toWavelength());
		return quantity;
	}
	
	public abstract Wavelength toWavelength();
	
	public abstract void setValue(Wavelength wavelength);
	
	
	public Amount<T> getValue() {
		return amount;
	}

	
	@SuppressWarnings("unchecked")
	public void setValue(Amount<?> amount) {
		this.amount = (Amount<T>) amount;
	}
}