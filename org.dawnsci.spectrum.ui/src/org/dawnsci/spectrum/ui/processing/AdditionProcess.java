package org.dawnsci.spectrum.ui.processing;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

public class AdditionProcess extends AbstractCacheProcess {


	public AdditionProcess(IContain1DData add) {
		super(add);
	}
	
	@Override
	protected Dataset process(Dataset x, Dataset y) {
		Dataset y1 = DatasetUtils.convertToDataset(cachedData.getyDatasets().get(0));
		Dataset out = Maths.add(y, y1);
		out.setName(y.getName()+ "_add_"+y1.getName());
		return out;
	}

	
	@Override
	protected String getAppendingName() {
		return "+"+oCachedData.getName();
	}

}
