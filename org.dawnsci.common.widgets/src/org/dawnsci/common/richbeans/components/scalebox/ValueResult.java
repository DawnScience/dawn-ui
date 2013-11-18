/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.scalebox;

/**
 * Class used to contain value parsed from box.
 */
public class ValueResult {

	public ValueResult(final double d, final boolean b) {
		numericValue = d;
		matched      = b;
	}
	
	private double  numericValue;
	private boolean matched;
	/**
	 * @return Returns the numericValue.
	 */
	public double getNumericValue() {
		return numericValue;
	}
	/**
	 * @param numericValue The numericValue to set.
	 */
	public void setNumericValue(double numericValue) {
		this.numericValue = numericValue;
	}
	/**
	 * @return Returns the matched.
	 */
	public boolean isMatched() {
		return matched;
	}
	/**
	 * @param matched The matched to set.
	 */
	public void setMatched(boolean matched) {
		this.matched = matched;
	}
}
