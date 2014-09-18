package org.dawnsci.spectrum.ui.processing;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

public class MultiplicationProcess extends AbstractCacheProcess {

	public MultiplicationProcess(IContain1DData cache) {
		super(cache);
	}

	@Override
	protected Dataset process(Dataset x, Dataset y) {
		Dataset y1 = DatasetUtils.convertToDataset(cachedData.getyDatasets().get(0));
		Dataset out = Maths.multiply(y, y1);
		out.setName(y.getName()+ "_multiplied_"+y1.getName());
		return out;
	}

	@Override
	protected String getAppendingName() {
		return "x"+oCachedData.getName();
	}

}
