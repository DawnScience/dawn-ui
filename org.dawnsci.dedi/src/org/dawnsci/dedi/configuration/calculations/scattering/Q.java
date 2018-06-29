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
 * A class that represents the magnitude of a scattering vector.
 */
public class Q extends ScatteringQuantity<WaveNumber> {
	private static final String NAME = "q";
	/**
	 * SI.METRE.inverse(), i.e. 1/m.
	 */
	public static final Unit<WaveNumber> BASE_UNIT = SI.RECIPROCAL_METRE;

	private static final List<Unit<WaveNumber>> UNITS = Arrays.asList(new ProductUnit<WaveNumber>(UnitUtils.NANOMETRE.inverse()),
			new ProductUnit<WaveNumber>(NonSI.ANGSTROM.inverse()));

	/**
	 * Constructs a new Q with value == null.
	 */
	public Q(){
	}

	/**
	 * @param value - value in Q.BASE_UNIT.
	 */
	public Q(double value){
		super(UnitUtils.getQuantity(value, BASE_UNIT));
	}

	public Q(Quantity<WaveNumber> value) {
		super((WaveNumber) value);
	}

	@Override
	public Unit<WaveNumber> getBaseUnit(){
		return Q.BASE_UNIT;
	}

	@Override
	public List<Unit<WaveNumber>> getUnits(){
		return Q.UNITS;
	}

	/**
	 * @return A new Q with value set to a copy of this Q's value.
	 * 
	 * @throws NullPointerException If this Q's value is null.
	 */
	@Override
	public Q toQ() {
		return new Q(UnitUtils.copy(value));
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
		setValue(UnitUtils.copy(q.getValue()));
	}
}