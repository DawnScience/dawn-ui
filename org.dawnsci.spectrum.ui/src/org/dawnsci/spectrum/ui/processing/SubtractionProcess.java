package org.dawnsci.spectrum.ui.processing;

import org.dawnsci.spectrum.ui.file.IContain1DData;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class SubtractionProcess extends AbstractCacheProcess{
	
	double scale = 1;
	
	public SubtractionProcess(IContain1DData subtrahend) {
		super(subtrahend);
	}
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public double getScale() {
		return scale;
	}
	

	@Override
	protected AbstractDataset process(final AbstractDataset x, final AbstractDataset y) {
		AbstractDataset y1 = DatasetUtils.convertToAbstractDataset(cachedData.getyDatasets().get(0));
		AbstractDataset s = Maths.multiply(y1, scale);
		s = Maths.subtract(y, s);
		return s;
	}
	
	public IContain1DData getSubtrahend() {
		return cachedData;
	}

	@Override
	protected String getAppendingName() {
		return "-"+oCachedData.getName();
	}

}
