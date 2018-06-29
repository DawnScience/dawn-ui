package org.dawnsci.dedi.configuration.calculations.scattering;

import java.util.Arrays;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;

import si.uom.NonSI;
import si.uom.SI;

/**
 * A class to represent a scattering angle.
 */
public class DoubleTheta extends ScatteringQuantity<Angle> {
	private Quantity<Length> wavelength;
	public static final String NAME = "2\u03B8";
	/**
	 * SI.RADIAN
	 */
	public static final Unit<Angle> BASE_UNIT = SI.RADIAN;

	private static final Unit<Length> WAVELENGTH_BASE_UNIT = SI.METRE;
	private static final List<Unit<Angle>> UNITS = Arrays.asList(NonSI.DEGREE_ANGLE, SI.RADIAN);

	public DoubleTheta(){
	}

	/**
	 * @param wavelength - wavelength in metres.
	 */
	public DoubleTheta(double wavelength){
		this.wavelength = UnitUtils.getQuantity(wavelength, WAVELENGTH_BASE_UNIT);
	}

	public DoubleTheta(Quantity<Length> wavelength) {
		this.wavelength = wavelength;
	}

	/**
	 * @param doubleTheta - angle in DoubleTheta.BASE_UNIT.
	 * @param wavelength  - wavelength in metres.
	 */
	public DoubleTheta(double doubleTheta, double wavelength){
		super(UnitUtils.getQuantity(doubleTheta, BASE_UNIT));
		this.wavelength = UnitUtils.getQuantity(wavelength, WAVELENGTH_BASE_UNIT);
	}

	public DoubleTheta(Quantity<Angle> doubleTheta, Quantity<Length> wavelength) {
		super((Angle) doubleTheta);
		this.wavelength = wavelength;
	}

	@Override
	public Unit<Angle> getBaseUnit() {
		return BASE_UNIT;
	}

	@Override
	public List<Unit<Angle>> getUnits() {
		return UNITS;
	}

	/**
	 * @return A new Q with value set to this DoubleTheta's value converted to Q.
	 * 
	 * @throws NullPointerException If this DoubleTheta's value or wavelength are null.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Q toQ() {
		return new Q(4*Math.PI*Math.sin(value.to(BASE_UNIT).getValue().doubleValue()/2)/
				wavelength.to((Unit<Length>) Q.BASE_UNIT.inverse()).getValue().doubleValue());
	}

	@Override
	public String getQuantityName() {
		return NAME;
	}

	public Quantity<Length> getWavelength(){
		return wavelength;
	}

	/**
	 * @param wavelength - the new wavelength in metres.
	 */
	public void setWavelength(Double wavelength){
		this.wavelength = (wavelength == null) ? null : UnitUtils.getQuantity(wavelength, WAVELENGTH_BASE_UNIT);
	}

	/**
	 * @param wavelength - the new wavelength.
	 */
	public void setWavelength(Quantity<Length> wavelength){
		this.wavelength = wavelength;
	}

	/**
	 * Sets the value of this DoubleTheta to the value held by the given Q converted to DoubleTheta.
	 * Sets the value to NaN if the Q value is not attainable at this DoubleTheta's wavelength.
	 * Does not modify the given Q. 
	 * 
	 * @throws NullPointerException If the given Q is null, if its value is null, or if this DoubleTheta's wavelength is null.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setValue(Q q) {
		setValue(UnitUtils.getQuantity(2*Math.asin(q.getValue().to(Q.BASE_UNIT).getValue().doubleValue() *
			wavelength.to((Unit<Length>) Q.BASE_UNIT.inverse()).getValue().doubleValue()/(4*Math.PI)), BASE_UNIT));
	}
}