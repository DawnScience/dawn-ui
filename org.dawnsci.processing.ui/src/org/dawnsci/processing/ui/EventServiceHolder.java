package org.dawnsci.processing.ui;

import org.eclipse.scanning.api.event.IEventService;

public class EventServiceHolder {

	private static IEventService eService;

	public static IEventService getEventService() {
		return eService;
	}
	
	public void setEventService(IEventService eService) {
		EventServiceHolder.eService = eService;
	}
	
}
