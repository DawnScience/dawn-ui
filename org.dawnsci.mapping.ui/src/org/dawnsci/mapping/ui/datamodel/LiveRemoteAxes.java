package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.dawnsci.analysis.api.dataset.IRemoteDataset;

public class LiveRemoteAxes {

	private IRemoteDataset[] axes;
	private String[] axesNames;
	private IRemoteDataset xAxisForRemapping;
	private String xAxisForRemappingName;

	public LiveRemoteAxes(IRemoteDataset[] axes, String[] axesNames) {
		this.axes = axes;
		this.axesNames = axesNames;
	}
	
	public IRemoteDataset getxAxisForRemapping() {
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

	public void setxAxisForRemapping(IRemoteDataset ds){
		this.xAxisForRemapping = ds;
	}
	
	public IRemoteDataset[] getAxes() {
		return axes;
	}
	
}
