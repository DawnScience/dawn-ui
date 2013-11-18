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

import org.dawnsci.doe.DOEUtils;

/**
 * Vanilla class to act as a bean for a single value.
 * Not intended for external use.
 */
public class IntegerValueBean extends NumberValueBean{

	private Integer value;

	public IntegerValueBean() {
		
	}
	
	public IntegerValueBean(Integer value) {
		this.setValue(value);
	}

	/**
	 * @return Returns the value.
	 */
	@Override
	public Integer getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(Integer value) {
		this.value = value;
	}

	public static List<IntegerValueBean> getBeanList(String currentValue) {
		
		final List<IntegerValueBean> ret = new ArrayList<IntegerValueBean>(7);
		if (currentValue==null) return ret;
		
		final List<? extends Number> vals = DOEUtils.expand(currentValue);
		for (Number dbl : vals) ret.add(new IntegerValueBean(dbl.intValue()));
		
		return ret;
	}
	
	public static List<IntegerValueBean> getBeanList(List<? extends Number> vals) {
		final List<IntegerValueBean> ret = new ArrayList<IntegerValueBean>(vals.size());
		for (Number val : vals) ret.add(new IntegerValueBean(val.intValue()));
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
		IntegerValueBean other = (IntegerValueBean) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
