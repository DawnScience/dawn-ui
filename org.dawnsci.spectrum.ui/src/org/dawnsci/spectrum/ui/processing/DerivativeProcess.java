package org.dawnsci.spectrum.ui.processing;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class DerivativeProcess extends AbstractProcess {

	@Override
	public List<IContain1DData> process() {
		
		List<IContain1DData> output = new ArrayList<IContain1DData>();
		
		for (IContain1DData data : list) {
			
			List<IDataset> out = new ArrayList<IDataset>();
			
			AbstractDataset x = DatasetUtils.convertToAbstractDataset(data.getxDataset());
			
			for (IDataset y : data.getyDatasets()) {
				out.add(Maths.derivative(x, DatasetUtils.convertToAbstractDataset(y), 1));
			}
			
			output.add(new Contain1DDataImpl(x, out, data.getName() + "_derivative"));
		}
		
		return output;
	}

}
