package org.dawnsci.mapping.ui;

import org.eclipse.scanning.api.ui.IStageScanConfiguration;

public class AcquisitionServiceManager {

	private static IStageScanConfiguration stageConfiguration;

	public static IStageScanConfiguration getStageConfiguration() {
		return stageConfiguration;
	}

	public static void setStageConfiguration(IStageScanConfiguration stageConfiguration) {
		AcquisitionServiceManager.stageConfiguration = stageConfiguration;
	}
	
	
}
