package org.dawnsci.spectrum.ui.processing;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

public class MultiplyMinusOneProcess extends AbstractProcess {

	@Override
	protected Dataset process(Dataset x, Dataset y) {
		return Maths.multiply(y, -1);
	}

	@Override
	protected String getAppendingName() {
		return "_x-1";
	}
	
}
