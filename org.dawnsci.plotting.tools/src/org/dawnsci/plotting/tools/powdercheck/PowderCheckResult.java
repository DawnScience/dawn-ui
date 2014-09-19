/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.powdercheck;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;

public class PowderCheckResult {
	
	private IFunction peak;
	private double q;

	public PowderCheckResult(IFunction peak, double q) {
		this.peak = peak;
		this.q = q;
	}
	
	public IFunction getPeak() {
		return this.peak;
	}
	
	public double getCalibrantQValue() {
		return this.q;
	}

}
