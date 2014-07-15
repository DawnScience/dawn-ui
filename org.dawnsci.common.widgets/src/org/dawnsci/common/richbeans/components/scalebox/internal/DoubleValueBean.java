/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.scalebox.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.doe.DOEUtils;

/**
 * Vanilla class to act as a bean for a single value.
 * Not intended for external use.
 */
public class DoubleValueBean extends NumberValueBean {

	private Double value;

	public DoubleValueBean() {
		
	}
	
	public DoubleValueBean(Double value) {
		this.setValue(value);
	}

	/**
	 * @return Returns the value.
	 */
	@Override
	public Double getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(Double value) {
		this.value = value;
	}

	public static List<DoubleValueBean> getBeanList(String currentValue) {
		
		final List<DoubleValueBean> ret = new ArrayList<DoubleValueBean>(7);
		if (currentValue==null) return ret;
		
		final List<? extends Number> vals = DOEUtils.expand(currentValue);
		for (Number dbl : vals) ret.add(new DoubleValueBean(dbl.doubleValue()));
		
		return ret;
	}

	public static List<DoubleValueBean> getBeanList(List<? extends Number> vals) {
		final List<DoubleValueBean> ret = new ArrayList<DoubleValueBean>(vals.size());
		for (Number val : vals) ret.add(new DoubleValueBean(val.doubleValue()));
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DoubleValueBean other = (DoubleValueBean) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
