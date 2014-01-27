package org.dawnsci.plotting.tools.powdercheck;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

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
