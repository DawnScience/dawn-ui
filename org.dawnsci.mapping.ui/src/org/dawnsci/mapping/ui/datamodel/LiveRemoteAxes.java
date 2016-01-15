package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.dawnsci.analysis.api.dataset.IRemoteDataset;

public class LiveRemoteAxes {

	private IRemoteDataset[] axes;
	private IRemoteDataset xAxisForRemapping;

	public LiveRemoteAxes(IRemoteDataset[] axes) {
		this.axes = axes;
	}
	
	public IRemoteDataset getxAxisForRemapping() {
		return xAxisForRemapping;
	}
	
	public void setxAxisForRemapping(IRemoteDataset ds){
		this.xAxisForRemapping = ds;
	}
	
	public IRemoteDataset[] getAxes() {
		return axes;
	}
	
}
