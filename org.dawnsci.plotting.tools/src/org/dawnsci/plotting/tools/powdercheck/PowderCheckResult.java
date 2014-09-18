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
