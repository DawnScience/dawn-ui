package org.dawnsci.common.widgets.gda.function.jexl;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;

// TODO add connectors for rest of supported functions
// someone with more knowlede of Jexl may be able to write
// a generic version (using reflection?)
// In particular it is likely desirable that the JexlFunctionConnector
// support all functions that can be provided to the FunctionTreeViewer
public class JexlFunctionConnector {

	public static double Gaussian(double x, double p, double w, double a) {
		return new Gaussian(p, w, a).val(x);
	}

}
