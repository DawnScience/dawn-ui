package org.dawnsci.spectrum.ui.processing;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.optimize.ApachePolynomial;

public class PolySmoothProcess extends AbstractProcess {

	@Override
	protected Dataset process(Dataset x, Dataset y) {
		try {
			return ApachePolynomial.getPolynomialSmoothed(x,y,13,9);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	protected String getAppendingName() {
		return "_smooth";
	}

}
