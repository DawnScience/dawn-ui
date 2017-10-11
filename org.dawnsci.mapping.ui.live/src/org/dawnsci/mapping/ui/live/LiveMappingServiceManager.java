package org.dawnsci.mapping.ui.live;

import org.eclipse.scanning.api.event.IEventService;

public class LiveMappingServiceManager {
	
	private static IEventService iEventService;

	public static IEventService getIEventService() {
		return iEventService;
	}

	public static void setIEventService(IEventService iEventService) {
		LiveMappingServiceManager.iEventService = iEventService;
	}
	


}
