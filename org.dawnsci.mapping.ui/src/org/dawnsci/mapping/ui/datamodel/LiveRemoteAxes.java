package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.january.dataset.IDatasetConnector;

public class LiveRemoteAxes {

	private IDatasetConnector[] axes;
	private String[] axesNames;
	private IDatasetConnector xAxisForRemapping;
	
	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	private String xAxisForRemappingName;
	
	private int port;
	private String host;

	public LiveRemoteAxes(IDatasetConnector[] axes, String[] axesNames, String host, int port) {
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
