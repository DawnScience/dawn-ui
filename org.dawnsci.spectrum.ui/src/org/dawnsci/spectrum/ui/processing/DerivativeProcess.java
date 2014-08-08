package org.dawnsci.spectrum.ui.processing;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class DerivativeProcess extends AbstractProcess {

	@Override
	protected Dataset process(Dataset x, Dataset y) {
		return Maths.derivative(x, y, 1);
	}

	@Override
	protected String getAppendingName() {
		return "_derivative";
	}
}
