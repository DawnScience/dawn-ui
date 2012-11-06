package org.dawb.workbench.plotting.tools.diffraction;

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
	
	public Unit<?> getUnit() {
		if (value!=null)        return value.getUnit();
		if (defaultValue!=null) return defaultValue.getUnit();
		return defaultUnit;
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

}
