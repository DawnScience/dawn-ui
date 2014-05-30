package org.dawnsci.spectrum.ui.processing;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class MultiplyMinusOneProcess extends AbstractProcess {

	@Override
	protected AbstractDataset process(AbstractDataset x, AbstractDataset y) {
		return Maths.multiply(y, -1);
	}

	@Override
	protected String getAppendingName() {
		return "_x-1";
	}
	
}
