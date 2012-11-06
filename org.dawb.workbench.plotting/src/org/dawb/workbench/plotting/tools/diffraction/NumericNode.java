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
		this.defaultValue = Amount.valueOf(0, unit);
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

	public void setValue(Amount<E> value) {
		this.value = value;
	}
	
	public void setDefault(Amount<E> defaultValue) {
		this.defaultValue = defaultValue;
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
		return null;
	}

	public void setUnit(Unit<E> unit) {
		if (value!=null)        value        = Amount.valueOf(value.doubleValue(unit), unit);
		if (defaultValue!=null) defaultValue = Amount.valueOf(defaultValue.doubleValue(unit), unit);
	}
	
	public void reset() {
		value = null;
	}

}
