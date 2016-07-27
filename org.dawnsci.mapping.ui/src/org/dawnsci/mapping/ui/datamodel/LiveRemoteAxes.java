package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.january.dataset.IDatasetConnector;

public class LiveRemoteAxes {

	private IDatasetConnector[] axes;
	private String[] axesNames;
	private IDatasetConnector xAxisForRemapping;
	private String xAxisForRemappingName;

	public LiveRemoteAxes(IDatasetConnector[] axes, String[] axesNames) {
		this.axes = axes;
		this.axesNames = axesNames;
	}
	
	public IDatasetConnector getxAxisForRemapping() {
		return xAxisForRemapping;
	}
	
	public String getxAxisForRemappingName() {
		return xAxisForRemappingName;
	}

	public void setxAxisForRemappingName(String xAxisForRemappingName) {
		this.xAxisForRemappingName = xAxisForRemappingName;
	}

	public String[] getAxesNames() {
		return axesNames;
	}

	public void setxAxisForRemapping(IDatasetConnector ds){
		this.xAxisForRemapping = ds;
	}
	
	public IDatasetConnector[] getAxes() {
		return axes;
	}
	
}
