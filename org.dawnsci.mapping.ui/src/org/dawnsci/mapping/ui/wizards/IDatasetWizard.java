package org.dawnsci.mapping.ui.wizards;

import java.util.Map;

import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.dawnsci.mapping.ui.datamodel.MappedFileDescription;

public interface IDatasetWizard {
	
	public void setDatasetMaps(Map<String,int[]> datasetNames, Map<String,int[]> nexusDatasetNames);
	
	public void setMappedDataDescription(MappedFileDescription description);
	
	public void setMapBean(MappedDataFileBean bean);
	
}
