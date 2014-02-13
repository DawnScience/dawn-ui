package org.dawnsci.spectrum.ui.processing;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.function.Abs;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.dawnsci.spectrum.ui.utils.SpectrumUtils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class SubtractionProcess extends AbstractProcess{
	
	double scale = 1;
	IContain1DData oSubtrahend;
	IContain1DData subtrahend;
	
	public SubtractionProcess(IContain1DData subtrahend) {
		this.oSubtrahend = subtrahend;
	}
	
	public List<IContain1DData> process(List<IContain1DData> list) {
		
		list.add(oSubtrahend);
		
		List<IContain1DData> listCom = SpectrumUtils.getCompatibleDatasets(list);
		
		this.subtrahend = listCom.remove(listCom.size()-1);
		
		return super.process(listCom);
	}
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public double getScale() {
		return scale;
	}
	

	@Override
	protected AbstractDataset process(final AbstractDataset x, final AbstractDataset y) {
		AbstractDataset y1 = DatasetUtils.convertToAbstractDataset(subtrahend.getyDatasets().get(0));
		AbstractDataset s = Maths.multiply(y1, scale);
		s = Maths.subtract(y, s);
		return s;
	}
	
	public IContain1DData getSubtrahend() {
		return subtrahend;
	}

	@Override
	protected String getAppendingName() {
		return "-"+subtrahend.getName();
	}

}
