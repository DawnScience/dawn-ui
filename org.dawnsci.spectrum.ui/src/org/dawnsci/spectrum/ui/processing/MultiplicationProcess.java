package org.dawnsci.spectrum.ui.processing;

import org.dawnsci.spectrum.ui.file.IContain1DData;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

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
