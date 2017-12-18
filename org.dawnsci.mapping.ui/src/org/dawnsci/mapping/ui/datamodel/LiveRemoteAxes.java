package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;

public class LiveRemoteAxes {

	private IDynamicDataset[] axes;
	private String[] axesNames;
	private IDynamicDataset xAxisForRemapping;
	
	
	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	private String xAxisForRemappingName;
	
	private int port;
	private String host;

	public LiveRemoteAxes(IDynamicDataset[] axes, String[] axesNames, String host, int port) {
		this.axes = axes;
		this.axesNames = axesNames;
		this.host = host;
		this.port = port;
	}
	
	public ILazyDataset getxAxisForRemapping() {
		if (xAxisForRemapping == null) return null;
		return xAxisForRemapping.getDataset();
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

	public void setxAxisForRemapping(IDynamicDataset ds){
		this.xAxisForRemapping = ds;
	}
	
	public ILazyDataset[] getAxes() {
		
		ILazyDataset[] lz = new ILazyDataset[axes.length];
		for (int i = 0; i < axes.length; i++) {
			lz[i] = axes[i] != null ? axes[i].getDataset() : null;
		}
		
		return lz;
	}
	
	public void update() {
		updateReady(false);
	}
	
	public boolean isReady() {
		
		return updateReady(true);
	}
	
	private boolean updateReady(boolean breakOnFail) {
		
		boolean isReady = true;
		
		for (int i = 0; i < axes.length; i++) {
			IDynamicDataset ax = axes[i];
			if (ax != null) {
				boolean r = ax.refreshShape();
				ax.getDataset().setName(axesNames[i]);
				if (!r) {
					isReady = false;
					if (breakOnFail) return false;
				}
			}
		}
		
		if (xAxisForRemapping != null) {
			boolean r = xAxisForRemapping.refreshShape();
			xAxisForRemapping.setName(xAxisForRemappingName);
			if (!r) {
				isReady = false;
				if (breakOnFail) return false;
			}
		}
		
		return isReady;
		
	}
	
}
