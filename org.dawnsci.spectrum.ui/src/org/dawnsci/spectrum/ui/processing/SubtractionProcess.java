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
	boolean isAminusB = true;
	
	public SubtractionProcess() {
		makeCompatible = true;
	}
	
	public List<IContain1DData> process() {
		
		AbstractDataset y1 = DatasetUtils.convertToAbstractDataset(list.get(1).getyDatasets().get(0));
		AbstractDataset y0 = DatasetUtils.convertToAbstractDataset(list.get(0).getyDatasets().get(0));
		
		Contain1DDataImpl out = null;
		
		if (isAminusB) {
			AbstractDataset s = Maths.multiply(y1, scale);
			
			s = Maths.subtract(y0, s);
			out = new Contain1DDataImpl(list.get(0).getxDataset(),
					Arrays.asList(new IDataset[]{s}), s.getName());
		} else {
			AbstractDataset s = Maths.multiply(y0, scale);
			
			s = Maths.subtract(y1, s);
			
			out = new Contain1DDataImpl(list.get(0).getxDataset(),
					Arrays.asList(new IDataset[]{s}), s.getName());
		}
		
		return Arrays.asList(new IContain1DData[]{out});
	}
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public double getScale() {
		return scale;
	}
	
	public void setAminusB(boolean isAminusB) {
		this.isAminusB = isAminusB;
	}
	
	public boolean isAminusB() {
		return isAminusB;
	}

}
