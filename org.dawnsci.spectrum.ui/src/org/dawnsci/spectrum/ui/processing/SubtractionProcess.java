package org.dawnsci.spectrum.ui.processing;

import org.dawnsci.spectrum.ui.file.IContain1DData;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
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
	protected Dataset process(final Dataset x, final Dataset y) {
		Dataset y1 = DatasetUtils.convertToDataset(cachedData.getyDatasets().get(0));
		Dataset s = Maths.multiply(y1, scale);
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
