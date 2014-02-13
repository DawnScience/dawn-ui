package org.dawnsci.spectrum.ui.processing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.function.Abs;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.dawnsci.spectrum.ui.utils.SpectrumUtils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public abstract class AbstractProcess {
	
	public List<IContain1DData> process(List<IContain1DData> list) {
	
	List<IContain1DData> output = new ArrayList<IContain1DData>();
	
	for (IContain1DData data : list) {
		
		List<IDataset> out = new ArrayList<IDataset>();
		
		AbstractDataset x = DatasetUtils.convertToAbstractDataset(data.getxDataset());
		
		for (IDataset y : data.getyDatasets()) {
			out.add(process(x, DatasetUtils.convertToAbstractDataset(y)));
		}
		
		output.add(new Contain1DDataImpl(x, out, data.getName() + getAppendingName(), data.getLongName() + getAppendingName()));
	}
	
	return output;
}
	
	protected abstract AbstractDataset process(AbstractDataset x, AbstractDataset y);
	
	protected abstract String getAppendingName();
	
//	public List<IContain1DData> getDatasetList() {
//		return list;
//	}

}
