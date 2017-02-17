package org.dawnsci.datavis;

import org.osgi.service.event.EventAdmin;

public class ServiceManager {

	private static EventAdmin eventAdmin;
	
	public static EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	public static void setEventAdmin(EventAdmin eventAdmin) {
		ServiceManager.eventAdmin = eventAdmin;
	}
	
}
