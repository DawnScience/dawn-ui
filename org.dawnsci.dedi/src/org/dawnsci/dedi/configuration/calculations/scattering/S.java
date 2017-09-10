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
 * S := 1/Q
 * 
 * @see Q
 */
public class S extends ScatteringQuantity<InverseLength> {
	public static final String NAME = "s";
	/**
	 * SI.METER.inverse(), i.e. 1/m.
	 */
	public static final Unit<InverseLength> BASE_UNIT = new ProductUnit<>(SI.METER.inverse()); 
	private static final List<Unit<InverseLength>> UNITS = 
			new ArrayList<>(Arrays.asList(new ProductUnit<InverseLength>(SI.NANO(SI.METER).inverse()), 
					                      new ProductUnit<InverseLength>(NonSI.ANGSTROM.inverse())));
	
	public S(){
	}
	
	
	/**
	 * @param value - value in S.BASE_UNIT.
	 */
	public S(double value){
		super(Amount.valueOf(value, BASE_UNIT));
	}
	
	
	public S(Amount<InverseLength> value) {
		super(value);
	}
	
	
	@Override
	public Unit<InverseLength> getBaseUnit(){
		return S.BASE_UNIT;
	}
	
	
	@Override
	public List<Unit<InverseLength>> getUnits(){
		return S.UNITS;
	}
	

	@Override
	public Q toQ() {
		return new Q(this.getValue().times(Math.PI*2).to(Q.BASE_UNIT));
	}

	@Override
	public String getQuantityName() {
		return NAME;
	}

	@Override
	public void setValue(Q q) {
		setValue(q.getValue().divide(Math.PI*2));
	}
}