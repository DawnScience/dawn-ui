package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveRemoteAxes {

	private IDatasetConnector[] axes;
	private String[] axesNames;
	private IDatasetConnector xAxisForRemapping;
	
	private static final Logger logger = LoggerFactory.getLogger(LiveRemoteAxes.class);
	
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

	public void setxAxisForRemapping(IDatasetConnector ds){
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
		for (int i = 0; i < axes.length; i++) {
			IDatasetConnector ax = axes[i];
			if (ax != null) {
				ax.refreshShape();
				ax.getDataset().setName(axesNames[i]);
			}
		}
		
		if (xAxisForRemapping != null) {
			xAxisForRemapping.refreshShape();
			xAxisForRemapping.getDataset().setName(xAxisForRemappingName);
		}
	}
	
	public boolean connect(boolean connect) {
		
		boolean success = true;
		
		for (IDatasetConnector a : axes) {
			if (a instanceof IDatasetConnector) {
				
				try {
					if (connect) a.connect();
					else a.disconnect();
				}
				catch (Exception e) {
					logger.error("Error communicating with " + a.getDataset().getName());
					success = false;
				} 
			}
		}
		
		if (xAxisForRemapping != null) {
			try {
				if (connect) xAxisForRemapping.connect();
				else xAxisForRemapping.disconnect();
			}
			catch (Exception e) {
				logger.error("Error communicating with " + xAxisForRemapping.getDataset().getName());
				success = false;
			} 
		}
		
		return success;
	}
	
}
