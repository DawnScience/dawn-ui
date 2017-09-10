package org.dawnsci.dedi.configuration.calculations.scattering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

/**
 * D := 2*pi/Q
 *
 * @see Q
 */
public class D extends ScatteringQuantity<Length> {
	public static final String NAME = "d";
	/**
	 * SI.METER
	 */
	public static final Unit<Length> BASE_UNIT = SI.METER;
	private static final List<Unit<Length>> UNITS = 
			new ArrayList<>(Arrays.asList(SI.NANO(SI.METER), 
					                      NonSI.ANGSTROM));
	
	public D(){
	}
	
	
	public D(Amount<Length> value) {
		super(value);
	}
	
	
	/**
	 * @param value - value in D.BASE_UNIT.
	 */
	public D(double value){
		super(Amount.valueOf(value, BASE_UNIT));
	}
	
	
	@Override
	public Unit<Length> getBaseUnit(){
		return D.BASE_UNIT;
	}
	
	
	@Override
	public List<Unit<Length>> getUnits(){
		return D.UNITS;
	}


	@Override
	public Q toQ() {
		return new Q(this.getValue().inverse().times(2*Math.PI).to(Q.BASE_UNIT));
	}

	
	@Override
	public String getQuantityName() {
		return NAME;
	}

	
	@Override
	public void setValue(Q q) {
		setValue(q.getValue().inverse().times(2*Math.PI));
	}
}