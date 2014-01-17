package org.dawnsci.spectrum.ui.processing;

import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.SpectrumUtils;

public abstract class AbstractProcess {
	
	protected boolean makeCompatible = false;
	List<IContain1DData> list;
	
	public void setDatasetList(List<IContain1DData> list) {
		if (makeCompatible) 
			this.list = SpectrumUtils.getCompatibleDatasets(list);
		else
			this.list = list;
	}
	
	public abstract List<IContain1DData> process();
	
	public List<IContain1DData> getDatasetList() {
		return list;
	}

}
