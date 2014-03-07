package org.dawnsci.common.widgets.gda.function.jexl;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;

/**
 * Reference implementation for a function to be accessible in jexl function expression
 */
public class JexlFunctionConnector {

	public static double Gaussian(double x, double p, double w, double a) {
		return new Gaussian(p, w, a).val(x);
	}

}
