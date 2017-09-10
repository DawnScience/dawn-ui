package org.dawnsci.dedi.configuration.calculations.scattering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;


/**
 * A class that represents the magnitude of a scattering vector.
 */
public class Q extends ScatteringQuantity<InverseLength> {
	private static final String NAME = "q";
	/**
	 * SI.METER.inverse(), i.e. 1/m.
	 */
	public static final Unit<InverseLength> BASE_UNIT = new ProductUnit<>(SI.METER.inverse());
	private static final List<Unit<InverseLength>> UNITS = 
			new ArrayList<>(Arrays.asList(new ProductUnit<InverseLength>(SI.NANO(SI.METER).inverse()), 
					                      new ProductUnit<InverseLength>(NonSI.ANGSTROM.inverse())));
	
	/**
	 * Constructs a new Q with value == null.
	 */
	public Q(){
	}
	
	
	/**
	 * @param value - value in Q.BASE_UNIT.
	 */
	public Q(double value){
		super(Amount.valueOf(value, BASE_UNIT));
	}
	
	
	public Q(Amount<InverseLength> value) {
		super(value);
	}
	
	
	@Override
	public Unit<InverseLength> getBaseUnit(){
		return Q.BASE_UNIT;
	}
	
	
	@Override
	public List<Unit<InverseLength>> getUnits(){
		return Q.UNITS;
	}
	

	/**
	 * @return A new Q with value set to a copy of this Q's value.
	 * 
	 * @throws NullPointerException If this Q's value is null.
	 */
	@Override
	public Q toQ() {
		return new Q(this.value.copy());
	}


	@Override
	public String getQuantityName() {
		return NAME;
	}

	
	/**
	 * Sets the value of this Q to a copy of the value held by the given Q.
	 * Does not modify the given Q.
	 * 
	 * @throws NullPointerException If the given Q is null, or if its value is null.
	 */	
	@Override
	public void setValue(Q q) {
		setValue(q.getValue().copy());
	}
}