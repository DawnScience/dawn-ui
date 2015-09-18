package org.dawnsci.mapping.ui.wizards;

import java.util.Map;

import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;

public interface IDatasetWizard {
	
	public void setDatasetMaps(Map<String,int[]> datasetNames, Map<String,int[]> nexusDatasetNames);
	
	public void setMapBean(MappedDataFileBean bean);
	
}
