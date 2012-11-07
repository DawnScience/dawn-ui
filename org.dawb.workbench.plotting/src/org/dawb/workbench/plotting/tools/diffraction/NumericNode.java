package org.dawb.workbench.plotting.tools.diffraction;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

/**
 * This class may be used with TreeNodeContentProvider to create a Tree of editable
 * items.
 * 
 * @author fcp94556
 *
 */
public class NumericNode<E extends Quantity> extends LabelNode {
		
	private Amount<E>  value;
	private Amount<E>  defaultValue;
	private Amount<E>  lowerBound;
	private Amount<E>  upperBound;
	private Unit<E>    defaultUnit;
	private double     increment;
	private NumberFormat format;
	
	/**
	 * allowedUnits does not have to be E intentionally
	 * For instance angstrom and eV are compatible using jscience
	 * which knows about the Planck constant relatationship.
	 */
	@SuppressWarnings("rawtypes")
	private List<Unit> allowedUnits;

	/**
	 * Unit must not be null.
	 * @param label
	 * @param unit, nit null
	 */
	public NumericNode(String label, Unit<E> unit) {
		this(label, null, unit);
	}
	
	/**
	 * Unit must not be null.
	 * @param label
	 * @param unit
	 */
	public NumericNode(String label, LabelNode parent, Unit<E> unit) {
		super(label, parent);
		this.defaultUnit = unit;
		this.increment   = 0.1;
		this.format  = new DecimalFormat("#0.####");
	}

    /**
     * The double value in the current unit set.
     * @return
     */
	public double getValue() {
		if (value!=null)        return value.doubleValue(value.getUnit());
		if (defaultValue!=null) return defaultValue.doubleValue(defaultValue.getUnit());
		return Double.NaN;
	}
	public String getValue(boolean isFormat) {
		if (isFormat) {
			return format.format(getValue());
		} else {
			return String.valueOf(getValue());
		}
	}

	public void setValue(double val) {
		if (value!=null)        {
			value = Amount.valueOf(val, value.getUnit());
			return;
		}
		if (defaultValue!=null) {
			value = Amount.valueOf(val, defaultValue.getUnit());
			return;
		}
		value = Amount.valueOf(val, defaultUnit);
		return;
	}
	public void setValue(Amount<E> value) {
		this.value = value;
	}
	
	/**
	 * Sets the default value and sets the bounds to
	 * the values 0 and 10000.
	 * @param value
	 */
	public void setDefault(double value, Unit<E> unit) {
		if (Double.isNaN(value)) return;// The value is NaN, doing Amount.valueOf(...) would set to 0
		this.defaultValue = Amount.valueOf(value, unit);
		this.lowerBound   = Amount.valueOf(Integer.MIN_VALUE, unit);
		this.upperBound   = Amount.valueOf(Integer.MAX_VALUE, unit);
	}

    /**
     * The double value in the current unit set.
     * @return
     */
	public double getDefaultValue() {
		if (defaultValue!=null) return defaultValue.doubleValue(defaultValue.getUnit());
		return Double.NaN;
	}
	public String getDefaultValue(boolean isFormat) {
		if (isFormat) {
			return format.format(getDefaultValue());
		} else {
			return String.valueOf(getDefaultValue());
		}
	}
	
	public Unit<?> getUnit() {
		if (value!=null)        return value.getUnit();
		if (defaultValue!=null) return defaultValue.getUnit();
		return defaultUnit;
	}
	
	public int getUnitIndex() {
		if (allowedUnits==null) return 0;
		return allowedUnits.indexOf(getUnit());
	}
	public void setUnitIndex(int index) {
		if (allowedUnits==null) return;
		final Unit to = allowedUnits.get(index);
		if (value!=null)        value        = value.to(to);
		if (defaultValue!=null) defaultValue = defaultValue.to(to);
	}

	public void setUnit(Unit<E> unit) {
		if (value!=null)        value        = Amount.valueOf(value.doubleValue(unit), unit);
		if (defaultValue!=null) defaultValue = Amount.valueOf(defaultValue.doubleValue(unit), unit);
	}
	
	public void reset() {
		value = null;
	}

	public Amount<E> getLowerBound() {
		return lowerBound;
	}
	public double getLowerBoundDouble() {
		return lowerBound.doubleValue(lowerBound.getUnit());
	}

	public void setLowerBound(Amount<E> lowerBound) {
		this.lowerBound = lowerBound;
	}
	
	public void setLowerBound(double lb) {
		this.lowerBound = Amount.valueOf(lb, defaultValue.getUnit());
	}

	public Amount<E> getUpperBound() {
		return upperBound;
	}
	public double getUpperBoundDouble() {
		return upperBound.doubleValue(upperBound.getUnit());
	}

	public void setUpperBound(Amount<E> upperBound) {
		this.upperBound = upperBound;
	}
	
	public void setUpperBound(double ub) {
		this.upperBound = Amount.valueOf(ub, defaultValue.getUnit());
	}

	public double getIncrement() {
		return increment;
	}

	public void setIncrement(double increment) {
		this.increment = increment;
	}

	public NumberFormat getFormat() {
		return format;
	}

	public void setFormat(NumberFormat format) {
		this.format = format;
	}
	public void setFormat(String format) {
		this.format = new DecimalFormat(format);
	}

	@SuppressWarnings("rawtypes")
	public List<Unit> getUnits() {
		return allowedUnits;
	}

	public void setUnits(@SuppressWarnings("rawtypes") List<Unit> allowedUnits) {
		this.allowedUnits = allowedUnits;
	}
	public void setUnits(@SuppressWarnings("rawtypes") Unit... allowedUnits) {
		this.allowedUnits = Arrays.asList(allowedUnits);
	}

	public String[] getUnitsString() {
		final String[] ret = new String[allowedUnits.size()];
		for (int i = 0; i < ret.length; i++) ret[i] = allowedUnits.get(i).toString();
		return ret;
	}

}
