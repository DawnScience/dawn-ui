package org.dawnsci.dedi.configuration.calculations.scattering;

import java.util.Arrays;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;

import si.uom.NonSI;
import si.uom.SI;
import si.uom.quantity.WaveNumber;
import tec.units.indriya.unit.ProductUnit;

/**
 * S := 1/Q
 * 
 * @see Q
 */
public class S extends ScatteringQuantity<WaveNumber> {
	public static final String NAME = "s";
	/**
	 * SI.METER.inverse(), i.e. 1/m.
	 */
	public static final Unit<WaveNumber> BASE_UNIT = SI.RECIPROCAL_METRE;

	private static final List<Unit<WaveNumber>> UNITS =
			Arrays.asList(new ProductUnit<WaveNumber>(UnitUtils.NANOMETRE.inverse()),
					new ProductUnit<WaveNumber>(NonSI.ANGSTROM.inverse()));

	public S(){
	}

	/**
	 * @param value - value in S.BASE_UNIT.
	 */
	public S(double value){
		super(UnitUtils.getQuantity(value, BASE_UNIT));
	}

	public S(Quantity<WaveNumber> value) {
		super(value);
	}

	@Override
	public Unit<WaveNumber> getBaseUnit(){
		return S.BASE_UNIT;
	}

	@Override
	public List<Unit<WaveNumber>> getUnits(){
		return S.UNITS;
	}

	@Override
	public Q toQ() {
		return new Q(getValue().multiply(Math.PI*2).to(Q.BASE_UNIT));
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