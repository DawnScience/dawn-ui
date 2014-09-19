/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.tick;

/**
 *
 */
public enum TickFormatting {
	/**
	 * Plain mode no rounding no chopping maximum 6 figures before the 
	 * fraction point and four after
	 */
	plainMode,
	/**
	 * Rounded or chopped to the nearest decimal
	 */
	roundAndChopMode,
	/**
	 * Use Exponent 
	 */
	useExponent,
	/**
	 * Use SI units (k,M,G,etc.)
	 */
	useSIunits
}
