/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.event;

import java.util.EventObject;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.swt.widgets.Widget;

/**
 * @author fcp94556
 *
 */
public class BoundsEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9067771454798050847L;

	/**
	 *
	 */
	public enum Mode {/**
	 * 
	 */
	GREATER, /**
	 * 
	 */
	LESS, /**
	 * 
	 */
	LEGAL}
	
	private Mode mode;
	/**
	 * @return the mode
	 */
	public Mode getMode() {
		return mode;
	}
	/**
	 * @param mode the mode to set
	 */
	public void setMode(final Mode mode) {
		this.mode = mode;
	}
	private double upper, lower, value;
	/**
	 * @return the upper
	 */
	public double getUpper() {
		return upper;
	}
	/**
	 * @param upper the upper to set
	 */
	public void setUpper(final double upper) {
		this.upper = upper;
	}
	/**
	 * @return the lower
	 */
	public double getLower() {
		return lower;
	}
	/**
	 * @param lower the lower to set
	 */
	public void setLower(final double lower) {
		this.lower = lower;
	}
	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(final double value) {
		this.value = value;
	}
	/**
	 * Constructor
	 * @param source
	 */
	public BoundsEvent(final Widget source) {
		super(source);
	}

	/**
	 * Just use beans method for summarising string.
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (final Exception e) {
			return e.getMessage();
		}
	}

}

	