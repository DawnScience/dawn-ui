/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.swing.tree.TreeNode;

import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;

import si.uom.NonSI;
import tec.units.indriya.quantity.Quantities;

/**
 * This class may be used with TreeNodeContentProvider to create a Tree of editable
 * items.
 * 
 * The classes LabelNode, NumericNode and ObjectNode are generic and may be used 
 * elsewhere. They have not been moved somewhere generic yet because they create a 
 * dependency on jscience.
 * 
 * @author Matthew Gerring
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class NumericNode<E extends Quantity<E>> extends LabelNode {
	private Quantity<E> value;  // Intentionally not E
	private Quantity<E> defaultValue;
	private Quantity<E> lowerBound;
	private Quantity<E> upperBound;
	private Unit<E> defaultUnit;
	private double  increment;

	/**
	 * allowedUnits does not have to be E intentionally
	 * For instance angstrom and eV are compatible using unit-api
	 * which knows about the Planck constant relationship.
	 */
	private List<Unit> allowedUnits;
	private String format;

	/**
	 * Unit must not be null.
	 * @param label
	 * @param unit
	 */
	public NumericNode(String label, LabelNode parent, Unit<E> unit) {
		this(label, parent, unit, "#0.####");
	}

	public NumericNode(String label, LabelNode parent, Unit<E> unit, String numberFormat) {
		super(label, parent);
		this.defaultUnit  = unit;
		this.increment    = 0.1;
		this.format = numberFormat;
	}

	public boolean isNaN() {
		return value==null&&defaultValue==null;
	}

	/**
	 * The double value in the current unit set.
	 * @return quantity
	 */
	public Quantity<E> getValue() {
		if (value!=null)        return value;
		if (defaultValue!=null) return defaultValue;
		return null;
	}

	public double getValue(Unit requiredUnit) {
		Quantity<E> val=getValue();
		if (isInAngstroms(val, requiredUnit)) {	
			return UnitUtils.convert(UnitUtils.h.multiply(UnitUtils.c).divide(val), requiredUnit);
		} else {
			if (val!=null) {
				return UnitUtils.convert(val, requiredUnit);
			}
		}

		return Double.NaN;
	}

	public double getDoubleValue() {
		final Quantity<E> val = getValue();
		return  val!=null? val.getValue().doubleValue() : Double.NaN;
	}

	public double getDefaultDoubleValue() {
		final Quantity<E> val = getDefaultValue();
		return  val!=null? val.getValue().doubleValue() : Double.NaN;
	}

	public void setDoubleValue(double val) {
		if (value!=null)        {
			value = UnitUtils.getQuantity(val, value.getUnit());
			fireAmountChanged(value);
			return;
		}
		if (defaultValue!=null) {
			value = UnitUtils.getQuantity(val, defaultValue.getUnit());
			fireAmountChanged(value);
			return;
		}
		value = UnitUtils.getQuantity(val, defaultUnit);
		fireAmountChanged(value);
		return;
	}

	private Collection<AmountListener<E>> listeners;

	protected void fireAmountChanged(Quantity<E> value, AmountListener<E>... ignored) {
		if (listeners==null) return;

		Collection<AmountListener<E>> informees;
		if (ignored == null || ignored.length == 0) {
			informees = listeners;
		} else {
			informees = new HashSet<AmountListener<E>>(listeners);
			informees.removeAll(Arrays.asList(ignored));
		}
		if (informees.size() == 0)
			return;

		final AmountEvent<E> evt = new AmountEvent<E>(this, value);
		for (AmountListener<E> l : informees) {
			l.amountChanged(evt);
		}
	}

	public void addAmountListener(AmountListener<E> l) {
		if (listeners==null) listeners = new HashSet<AmountListener<E>>(3);
		listeners.add(l);
	}

	public void removeAmountListener(AmountListener<E> l) {
		if (listeners==null) return;
		listeners.remove(l);
	}

	private Collection<UnitListener> unitListeners;

	protected void fireUnitChanged(Unit<E> unit) {
		if (unitListeners==null) return;
		final UnitEvent<E> evt = new UnitEvent<E>(this, unit);
		for (UnitListener l : unitListeners) {
			l.unitChanged(evt);
		}
	}

	public void addUnitListener(UnitListener l) {
		if (unitListeners==null) unitListeners = new HashSet<UnitListener>(3);
		unitListeners.add(l);
	}

	public void removeUnitListener(UnitListener l) {
		if (listeners==null) return;
		unitListeners.remove(l);
	}

	/**
	 * May be null
	 * @param val
	 */
	public void setValueQuietly(Quantity<E> val) {
		value = val;
	}

	/**
	 * May be null
	 * @param val
	 */
	public void setValue(Quantity<E> val) {
		setValue(val, (AmountListener<E>[]) null);
	}

	/**
	 * May be null
	 * @param val
	 * @param ignored amount listeners
	 */
	public void setValue(Quantity<E> val, AmountListener<E>... ignored) {
		Quantity<E> oldValue = value;
		setValueQuietly(val);
		if (value != null) {
			fireAmountChanged(val, ignored);
			Unit oldUnit = oldValue != null ? oldValue.getUnit() : (defaultValue != null ? defaultValue.getUnit() : defaultUnit);
			Unit unit = val.getUnit();
			if (!oldUnit.equals(unit))
				fireUnitChanged(unit);
		}
	}

	/**
	 * @param val
	 * @param unit
	 * @return unit used
	 */
	public void setValueQuietly(double val, Unit<E> unit) {
		if (Double.isNaN(val)) {
			value=null;
			return;// The value is NaN, doing Quantities.getQuantity(...) would set to 0
		}
		if (unit==null) {
			unit = value != null ? value.getUnit() : (defaultValue != null ? defaultValue.getUnit() : defaultUnit);
		}
		value = UnitUtils.getQuantity(val, unit);
	}

	/**
	 * @param val
	 * @param unit
	 */
	public void setValue(double val, Unit<E> unit) {
		setValue(val, unit, (AmountListener<E>[]) null);
	}

	/**
	 * @param val
	 * @param unit
	 * @param ignored amount listeners
	 */
	public void setValue(double val, Unit<E> unit, AmountListener<E>... ignored) {
		Quantity<E> oldValue = value;
		setValueQuietly(val, unit);
		if (value == null)
			return;

		fireAmountChanged(value, ignored);
		Unit oldUnit = oldValue != null ? oldValue.getUnit() : (defaultValue != null ? defaultValue.getUnit() : defaultUnit);
		unit = value.getUnit();
		if (!oldUnit.equals(unit))
			fireUnitChanged(unit);
	}

	public boolean mergeValue(TreeNode node) throws Throwable {
		if (equals(node)) return false;

		E newValue = null;
		if (node instanceof ObjectNode) {
			ObjectNode on = (ObjectNode)node;
			newValue = parseValue(on.getValue());
		}
		if (node instanceof NumericNode) {
			NumericNode nn = (NumericNode)node;
			newValue = (E) nn.getValue();
		}
		
		if (newValue!=null) {
			setValue(newValue);
			return true;
		}
		return false;
	}

	private E parseValue(Object val) throws Throwable {
		try {
			if (val instanceof Quantity) return (E) val;

			final double dbl = Double.parseDouble(val.toString());
			return UnitUtils.getQuantity(dbl, getValue().getUnit());
		} catch (Throwable ne) {
			try {
				return (E) Quantities.getQuantity(val.toString()); //e.g. "100.0 mm"
			} catch (Throwable e) {
				throw e;
			}
		}
	}

	public void setDefault(Quantity<E> amount) {
		this.defaultValue = amount;
	}

	/**
	 * Sets the default value and sets the bounds to
	 * the values 0 and 10000.
	 * @param value
	 */
	public void setDefault(double value, Unit<E> unit) {
		if (Double.isNaN(value)) return;// The value is NaN, doing Quantities.getQuantity(...) would set to 0
		this.defaultValue = UnitUtils.getQuantity(value, unit);
		this.lowerBound   = UnitUtils.getQuantity(Integer.MIN_VALUE, unit);
		this.upperBound   = UnitUtils.getQuantity(Integer.MAX_VALUE, unit);
	}

	/**
	 * The double value in the current unit set.
	 * @return quantity
	 */
	public Quantity<E> getDefaultValue() {
		if (defaultValue!=null) return defaultValue;
		return UnitUtils.getQuantity(Double.NaN, getUnit());
	}

	public Unit<E> getDefaultUnit() {
		return defaultUnit;
	}

	public Unit<E> getUnit() {
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
		if (value==null&&defaultValue!=null) {
			value = UnitUtils.copy(defaultValue);
		}
		if (value!=null) {
			// BODGE for A and eV !
			if (isInAngstroms(value, to)) {
				value = (E) UnitUtils.h.multiply(UnitUtils.c).divide(value).to(to);
			} else {
				value = (E) value.to(to);
			}
			fireUnitChanged(to);
		}
	}

	private boolean isInAngstroms(Quantity<?> val, Unit to) {
		boolean isAngstrom = allowedUnits!=null && allowedUnits.contains(NonSI.ANGSTROM) && allowedUnits.contains(NonSI.ELECTRON_VOLT);
		if (!isAngstrom) return false;
		return !val.getUnit().isCompatible(to); // Only convert incompatible.
	}

	public void setUnit(Unit<E> unit) {
		if (value!=null)        value        = UnitUtils.getQuantity(value.getValue(), unit);
		if (defaultValue!=null) defaultValue = UnitUtils.getQuantity(defaultValue.getValue(), unit);
		fireUnitChanged(unit);
	}

	public void reset() {
		value = null;
		fireAmountChanged(getValue());
	}

	public Quantity<E> getLowerBound() {
		return lowerBound;
	}

	public double getLowerBoundDouble() {
		return lowerBound.getValue().doubleValue();
	}

	public void setLowerBound(Quantity<E> lowerBound) {
		this.lowerBound = lowerBound;
	}
	
	public void setLowerBound(double lb) {
		this.lowerBound = UnitUtils.getQuantity(lb, getUnit());
	}

	public Quantity<E> getUpperBound() {
		return upperBound;
	}

	public double getUpperBoundDouble() {
		return upperBound.getValue().doubleValue();
	}

	public void setUpperBound(Quantity<E> upperBound) {
		this.upperBound = upperBound;
	}
	
	public void setUpperBound(double ub) {
		this.upperBound = UnitUtils.getQuantity(ub, getUnit());
	}

	public double getIncrement() {
		return increment;
	}

	public void setIncrement(double increment) {
		this.increment = increment;
	}

	public List<Unit> getUnits() {
		return allowedUnits;
	}

	public void setUnits(List<Unit> allowedUnits) {
		this.allowedUnits = allowedUnits;
	}

	public void setUnits(Unit... allowedUnits) {
		this.allowedUnits = Arrays.asList(allowedUnits);

		if (value!=null)        {
			value = convertToNewSet(value, allowedUnits);
			fireAmountChanged(value);
		}
		if (defaultValue!=null) defaultValue = convertToNewSet(defaultValue, allowedUnits);
	}

	private Quantity<E> convertToNewSet(Quantity<E> val, Unit[] au) {
		for (Unit unit : au) {
			// This unit is active and may have just 
			if (val.getUnit().toString().equals(unit.toString())) {
				Quantity<?> standard = val.to(val.getUnit());
				return (E) standard.to(unit);
			}
		}
		return val;
	}

	public String[] getUnitsString() {
		final String[] ret = new String[allowedUnits.size()];
		for (int i = 0; i < ret.length; i++) ret[i] = allowedUnits.get(i).toString();
		return ret;
	}

	public void dispose() {
		super.dispose();
		if (listeners!=null) listeners.clear();
		listeners = null;
		if (unitListeners!=null) unitListeners.clear();
		unitListeners = null;
		
		if (allowedUnits!=null) {
			try {
				allowedUnits.clear();
			} catch (Throwable ne) {
				// they are allowed unmodifiable units.
			}
		}
		allowedUnits = null;

		value        = null;
		defaultValue = null;
		lowerBound   = null;
		upperBound   = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((allowedUnits == null) ? 0 : allowedUnits.hashCode());
		result = prime * result
				+ ((defaultUnit == null) ? 0 : defaultUnit.hashCode());
		result = prime * result
				+ ((defaultValue == null) ? 0 : defaultValue.hashCode());
		long temp;
		temp = Double.doubleToLongBits(increment);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((lowerBound == null) ? 0 : lowerBound.hashCode());
		result = prime * result
				+ ((upperBound == null) ? 0 : upperBound.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NumericNode other = (NumericNode) obj;
		if (allowedUnits == null) {
			if (other.allowedUnits != null)
				return false;
		} else if (!allowedUnits.equals(other.allowedUnits))
			return false;
		if (defaultUnit == null) {
			if (other.defaultUnit != null)
				return false;
		} else if (!defaultUnit.equals(other.defaultUnit))
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (Double.doubleToLongBits(increment) != Double
				.doubleToLongBits(other.increment))
			return false;
		if (lowerBound == null) {
			if (other.lowerBound != null)
				return false;
		} else if (!lowerBound.equals(other.lowerBound))
			return false;
		if (upperBound == null) {
			if (other.upperBound != null)
				return false;
		} else if (!upperBound.equals(other.upperBound))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
