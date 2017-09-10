package org.dawnsci.dedi.configuration.calculations.scattering;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

/**
 * A class to represent a scattering angle.
 */
public class DoubleTheta extends ScatteringQuantity<Angle> {
	private Amount<Length> wavelength;
	public static final String NAME = "2\u03B8";
	/**
	 * SI.RADIAN
	 */
	public static final Unit<Angle> BASE_UNIT = SI.RADIAN;
	/**
	 * SI.METER
	 */
	private static final Unit<Length> WAVELENGTH_BASE_UNIT = SI.METER;
	private static final List<Unit<Angle>> UNITS = new ArrayList<>(Arrays.asList(NonSI.DEGREE_ANGLE, SI.RADIAN));
	
	
	public DoubleTheta(){
	}
	
	
	/**
	 * @param wavelength - wavelength in metres.
	 */
	public DoubleTheta(double wavelength){
		this.wavelength = Amount.valueOf(wavelength, WAVELENGTH_BASE_UNIT);
	}
	

	public DoubleTheta(Amount<Length> wavelength) {
		this.wavelength = wavelength;
	}
	
	
	/**
	 * @param doubleTheta - angle in DoubleTheta.BASE_UNIT.
	 * @param wavelength  - wavelength in metres.
	 */
	public DoubleTheta(double doubleTheta, double wavelength){
		super(Amount.valueOf(doubleTheta, BASE_UNIT));
		this.wavelength = Amount.valueOf(wavelength, WAVELENGTH_BASE_UNIT);
	}
	
	
	public DoubleTheta(Amount<Angle> doubleTheta, Amount<Length> wavelength) {
		super(doubleTheta);
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
	@Override
	public Q toQ() {
		return new Q(4*Math.PI*Math.sin(value.doubleValue(BASE_UNIT)/2)/
				     wavelength.to(Q.BASE_UNIT.inverse()).getEstimatedValue());
	}

	
	@Override
	public String getQuantityName() {
		return NAME;
	}
	
	
	public Amount<Length> getWavelength(){
		return wavelength;
	}
	
	
	/**
	 * @param wavelength - the new wavelength in metres.
	 */
	public void setWavelength(Double wavelength){
		this.wavelength = (wavelength == null) ? null : Amount.valueOf(wavelength, WAVELENGTH_BASE_UNIT);
	}
	
	
	/**
	 * @param wavelength - the new wavelength.
	 */
	public void setWavelength(Amount<Length> wavelength){
		this.wavelength = wavelength;
	}

	
	/**
	 * Sets the value of this DoubleTheta to the value held by the given Q converted to DoubleTheta.
	 * Sets the value to NaN if the Q value is not attainable at this DoubleTheta's wavelength.
	 * Does not modify the given Q. 
	 * 
	 * @throws NullPointerException If the given Q is null, if its value is null, or if this DoubleTheta's wavelength is null.
	 */
	@Override
	public void setValue(Q q) {
		setValue(Amount.valueOf(2*Math.asin(q.getValue().to(Q.BASE_UNIT).getEstimatedValue()*
				                wavelength.to(Q.BASE_UNIT.inverse()).getEstimatedValue()/(4*Math.PI)), BASE_UNIT));
	}
	
}