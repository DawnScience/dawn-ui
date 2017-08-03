/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
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

import org.eclipse.dawnsci.analysis.api.Constants;

import si.uom.NonSI;
import tec.uom.se.quantity.Quantities;

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
public class NumericNode<Q extends Quantity<?>> extends LabelNode {
			
	private Quantity     value;  // Intentionally not E
	private Quantity<?>  defaultValue;
	private Quantity<?>  lowerBound;
	private Quantity<?>  upperBound;
	private Unit<?>    defaultUnit;
	private double     increment;
	
	/**
	 * allowedUnits does not have to be E intentionally
	 * For instance angstrom and eV are compatible using jscience
	 * which knows about the Planck constant relationship.
	 */
	private List<Unit> allowedUnits;
	private String format;
	
	/**
	 * Unit must not be null.
	 * @param label
	 * @param unit
	 */
	public NumericNode(String label, LabelNode parent, Unit<?> unit) {
		this(label, parent, unit, "#0.####");
	}

	public NumericNode(String label, LabelNode parent, Unit<?> unit, String numberFormat) {
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
     * @return
     */
	public Quantity<?> getValue() {
		if (value!=null)        return value;
		if (defaultValue!=null) return defaultValue;
		return null;
	}
	
	public double getValue(Unit requiredUnit) {
		Quantity<?> val=getValue();
		if (isInAngstroms(val, requiredUnit)) {	
			return Constants.ℎ.multiply(Constants.c).divide(val).getValue().doubleValue();
		} else {
			if (val!=null) {
				return val.to(requiredUnit).getValue().doubleValue();
			}
		}

		return Double.NaN;
	}

	public double getDoubleValue() {
		final Quantity<?> val = getValue();
		return  val!=null? val.getValue().doubleValue() : Double.NaN;
	}
	
	public double getDefaultDoubleValue() {
		final Quantity<?> val = getDefaultValue();
		return  val!=null? val.getValue().doubleValue() : Double.NaN;
	}
	
	public void setDoubleValue(double val) {
		if (value!=null)        {
			value = Quantities.getQuantity(val, value.getUnit());
			fireAmountChanged(value);
			return;
		}
		if (defaultValue!=null) {
			value = Quantities.getQuantity(val, defaultValue.getUnit());
			fireAmountChanged(value);
			return;
		}
		value = Quantities.getQuantity(val, defaultUnit);
		fireAmountChanged(value);
		return;
	}
	
	private Collection<AmountListener<Q>> listeners;
	
	protected void fireAmountChanged(Quantity<?> value, AmountListener<?>... ignored) {
		if (listeners==null) return;

		Collection<AmountListener<Q>> informees;
		if (ignored == null || ignored.length == 0) {
			informees = listeners;
		} else {
			informees = new HashSet<AmountListener<Q>>(listeners);
			informees.removeAll(Arrays.asList(ignored));
		}
		if (informees.size() == 0)
			return;

		final AmountEvent<Q> evt = new AmountEvent<Q>(this, value);
		for (AmountListener<Q> l : informees) {
			l.amountChanged(evt);
		}
	}
	
	public void addAmountListener(AmountListener<Q> l) {
		if (listeners==null) listeners = new HashSet<AmountListener<Q>>(3);
		listeners.add(l);
	}
	
	public void removeAmountListener(AmountListener<?> l) {
		if (listeners==null) return;
		listeners.remove(l);
	}
	
	private Collection<UnitListener> unitListeners;
	
	protected void fireUnitChanged(Unit<?> unit) {
		if (unitListeners==null) return;
		final UnitEvent<?> evt = new UnitEvent<Q>(this, unit);
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
	public void setValueQuietly(Quantity<?> val) {
		value = val;
	}

	/**
	 * May be null
	 * @param val
	 */
	public void setValue(Quantity<?> val) {
		setValue(val, (AmountListener<?>[]) null);
	}

	/**
	 * May be null
	 * @param val
	 * @param ignored amount listeners
	 */
	public void setValue(Quantity<?> val, AmountListener<?>... ignored) {
		Quantity oldValue = value;
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
	public void setValueQuietly(double val, Unit<?> unit) {
		if (Double.isNaN(val)) {
			value=null;
			return;// The value is NaN, doing Amount.valueOf(...) would set to 0
		}
		if (unit==null) {
			unit = value != null ? value.getUnit() : (defaultValue != null ? defaultValue.getUnit() : defaultUnit);
		}
		value = Quantities.getQuantity(val, unit);
	}

	/**
	 * @param val
	 * @param unit
	 */
	public void setValue(double val, Unit<?> unit) {
		setValue(val, unit, (AmountListener<?>[]) null);
	}

	/**
	 * @param val
	 * @param unit
	 * @param ignored amount listeners
	 */
	public void setValue(double val, Unit<?> unit, AmountListener<?>... ignored) {
		Quantity oldValue = value;
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
		
		Quantity<?> newValue = null;
		if (node instanceof ObjectNode) {
			ObjectNode on = (ObjectNode)node;
			newValue = parseValue(on.getValue());
		}
		if (node instanceof NumericNode) {
			NumericNode nn = (NumericNode)node;
			newValue = nn.getValue();
		}
		
		if (newValue!=null) {
			setValue(newValue);
			return true;
		}
		return false;
	}
	
	private Quantity parseValue(Object val) throws Throwable {
		try {
			if (val instanceof Quantity) return (Quantity<?>)val;
			
			final double dbl = Double.parseDouble(val.toString());
			return Quantities.getQuantity(dbl, getValue().getUnit());
					
		} catch (Throwable ne) {
			try {
				return (Quantity)Quantities.getQuantity(val.toString()); //e.g. "100.0 mm"
				
			} catch (Throwable e) {
				throw e;
			}
		}
	}

	public void setDefault(Quantity<?> amount) {
		this.defaultValue = amount;
	}
	/**
	 * Sets the default value and sets the bounds to
	 * the values 0 and 10000.
	 * @param value
	 */
	public void setDefault(double value, Unit<?> unit) {
		if (Double.isNaN(value)) return;// The value is NaN, doing Amount.valueOf(...) would set to 0
		this.defaultValue = Quantities.getQuantity(value, unit);
		this.lowerBound   = Quantities.getQuantity(Integer.MIN_VALUE, unit);
		this.upperBound   = Quantities.getQuantity(Integer.MAX_VALUE, unit);
	}

    /**
     * The double value in the current unit set.
     * @return
     */
	public Quantity<?> getDefaultValue() {
		if (defaultValue!=null) return defaultValue;
		return Quantities.getQuantity(Double.NaN, getUnit());
	}
	
	public Unit<?> getDefaultUnit() {
		return defaultUnit;
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
		final Unit<?> to = allowedUnits.get(index);
		if (value==null&&defaultValue!=null) {
			value = Quantities.getQuantity(defaultValue.getValue(), defaultValue.getUnit());
		}
		if (value!=null) {
			// BODGE for A and eV !
			if (isInAngstroms(value, to)) {	
				value = Quantities.getQuantity(Constants.ℎ.multiply(Constants.c).divide(value).getValue().doubleValue(), to); 
			} else {
			    value = value.to(to);
			}
			fireUnitChanged(to);
		}
	}

	private boolean isInAngstroms(Quantity val, Unit to) {
		boolean isAngstrom = allowedUnits!=null && allowedUnits.contains(NonSI.ANGSTROM) && allowedUnits.contains(NonSI.ELECTRON_VOLT);
	    if (!isAngstrom) return false;
	    return !val.getUnit().isCompatible(to); // Only convert incompatible.
	}

	public void setUnit(Unit<?> unit) {
		if (value!=null)        value        = Quantities.getQuantity(value.to(unit).getValue().doubleValue(), unit);
		if (defaultValue!=null) defaultValue = Quantities.getQuantity(defaultValue.getValue().doubleValue(), unit);
		fireUnitChanged(unit);
	}

	public void reset() {
		value = null;
		fireAmountChanged(getValue());
	}

	public Quantity<?> getLowerBound() {
		return lowerBound;
	}
	public double getLowerBoundDouble() {
		return lowerBound.getValue().doubleValue();
	}

	public void setLowerBound(Quantity<?> lowerBound) {
		this.lowerBound = lowerBound;
	}
	
	public void setLowerBound(double lb) {
		this.lowerBound = Quantities.getQuantity(lb, getUnit());
	}

	public Quantity<?> getUpperBound() {
		return upperBound;
	}
	public double getUpperBoundDouble() {
		return upperBound.getValue().doubleValue();
	}

	public void setUpperBound(Quantity<?> upperBound) {
		this.upperBound = upperBound;
	}
	
	public void setUpperBound(double ub) {
		this.upperBound = Quantities.getQuantity(ub, getUnit());
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

	private Quantity<?> convertToNewSet(Quantity<?> val, Unit[] au) {

		for (Unit unit : au) {
			// This unit is active and may have just 
			if (val.getUnit().toString().equals(unit.toString())) {
				Quantity standard = Quantities.getQuantity(val.getValue().doubleValue(), val.getUnit());
				return standard.to(unit);
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
		
		value        =null;
		defaultValue =null;
		lowerBound   =null;
		upperBound   =null;
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
