package org.dawnsci.mapping.ui;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;

public class AcquisitionServiceManager {

	private static IStageScanConfiguration stageConfiguration;
	private static IEventService eventService;

	public static IStageScanConfiguration getStageConfiguration() {
		return stageConfiguration;
	}

	public static void setStageConfiguration(IStageScanConfiguration stageConfiguration) {
		AcquisitionServiceManager.stageConfiguration = stageConfiguration;
	}
	
	public static void setEventService(IEventService eService) {
		eventService = eService;
	}
	
	public static IEventService getEventService(){
		return eventService;
	}
}
