package org.dawnsci.dedi.configuration.calculations.scattering;

import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

public abstract class ScatteringQuantity<E extends Quantity>  {
	protected Amount<E> value;
	
	
	/**
	 * Constructs a scattering quantity with value == null.
	 */
	public ScatteringQuantity(){
	}
	
	
	/**
	 * Constructs a scattering quantity with value set to the given value.
	 */
	public ScatteringQuantity(Amount<E> value){
		this.value = value;
	}
	
	
	/**
	 * Converts the value stored in this quantity to the given scatteringQuantity,
	 * and sets the value of the given scatteringQuantity to the result of the conversion.
	 * 
	 * @return The given quantity, now holding the converted value.
	 * 
	 * @throws NullPointerException If the value stored in this quantity is null, or if the given scatteringQuantity is null.
	 */
	public <U extends Quantity, T extends ScatteringQuantity<U>> T to(T scatteringQuantity){
		scatteringQuantity.setValue(this.toQ());
		return scatteringQuantity;
	}
	
	
	/**
	 * @return A new Q with value set to this quantity's value converted to Q.
	 * 
	 * @throws NullPointerException If this quantity's value is null.
	 */
	public abstract Q toQ();
	
	
	public Amount<E> getValue(){
		return value;
	}
	
	
	/**
	 * Sets the value of this quantity to the given value.
	 * 
	 * @throws ClassCastException If the given Amount<?> is not compatible with the Quantity used for this ScatteringQuantity.
	 */
	@SuppressWarnings("unchecked")
	public void setValue(Amount<?> value){
		if(value != null) {
			if(!this.getBaseUnit().getDimension().equals(value.getUnit().getDimension())) throw new ClassCastException();
			this.value = (Amount<E>) value;
		} else {
			this.value = null;
		}
	}
	
	
	/**
	 * Sets the value of this quantity to the value held by the given Q converted to this quantity.
	 * Does not modify the given Q.
	 * 
	 * @throws NullPointerException If the given Q is null, or if its value is null.
	 */
	public abstract void setValue(Q q);
	
	public abstract Unit<E> getBaseUnit();
	
	public abstract List<Unit<E>> getUnits();
	
	public abstract String getQuantityName();
	
	
	@Override
	public String toString(){
		if(value == null) return "";
		return String.valueOf(value.toString());
	}	
}