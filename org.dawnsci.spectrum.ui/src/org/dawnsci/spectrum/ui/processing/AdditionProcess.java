package org.dawnsci.spectrum.ui.processing;

import org.dawnsci.spectrum.ui.file.IContain1DData;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class AdditionProcess extends AbstractCacheProcess {


	public AdditionProcess(IContain1DData add) {
		super(add);
	}
	
	@Override
	protected AbstractDataset process(AbstractDataset x, AbstractDataset y) {
		AbstractDataset y1 = DatasetUtils.convertToAbstractDataset(cachedData.getyDatasets().get(0));
		AbstractDataset out = Maths.add(y, y1);
		out.setName(y.getName()+ "_add_"+y1.getName());
		return out;
	}

	
	@Override
	protected String getAppendingName() {
		return "+"+oCachedData.getName();
	}

}
