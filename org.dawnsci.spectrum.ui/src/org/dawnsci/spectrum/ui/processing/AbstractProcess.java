package org.dawnsci.spectrum.ui.processing;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;

public abstract class AbstractProcess {
	
	public List<IContain1DData> process(List<IContain1DData> list) {

		List<IContain1DData> output = new ArrayList<IContain1DData>();

		for (IContain1DData data : list) {

			List<IDataset> out = new ArrayList<IDataset>();

			Dataset x = DatasetUtils.convertToDataset(data.getxDataset());

			for (IDataset y : data.getyDatasets()) {
				out.add(process(x, DatasetUtils.convertToDataset(y)));
			}

			output.add(new Contain1DDataImpl(x, out, data.getName() + getAppendingName(), data.getLongName() + getAppendingName()));
		}

		return output;
	}
	
	protected abstract Dataset process(Dataset x, Dataset y);
	
	protected abstract String getAppendingName();
	
//	public List<IContain1DData> getDatasetList() {
//		return list;
//	}

}
