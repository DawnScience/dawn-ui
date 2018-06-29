package org.dawnsci.dedi.configuration.calculations.scattering;

import java.util.Arrays;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;

import si.uom.NonSI;
import si.uom.SI;
import si.uom.quantity.WaveNumber;

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
	public static final Unit<Length> BASE_UNIT = SI.METRE;
	private static final List<Unit<Length>> UNITS = Arrays.asList(UnitUtils.NANOMETRE, NonSI.ANGSTROM);

	public D(){
	}

	public D(Quantity<Length> value) {
		super((Length) value);
	}

	/**
	 * @param value - value in D.BASE_UNIT.
	 */
	public D(double value){
		super(UnitUtils.getQuantity(value, BASE_UNIT));
	}

	@Override
	public Unit<Length> getBaseUnit() {
		return D.BASE_UNIT;
	}

	@Override
	public List<Unit<Length>> getUnits(){
		return D.UNITS;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Q toQ() {
		return new Q((Quantity<WaveNumber>) getValue().inverse().multiply(2*Math.PI));
	}

	@Override
	public String getQuantityName() {
		return NAME;
	}

	@Override
	public void setValue(Q q) {
		setValue(q.getValue().inverse().multiply(2*Math.PI));
	}
}