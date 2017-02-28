package org.dawnsci.datavis;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.osgi.service.event.EventAdmin;

public class ServiceManager {

	private static EventAdmin eventAdmin;
	private static IRecentPlaces places;
	
	public static EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	public static void setEventAdmin(EventAdmin eventAdmin) {
		ServiceManager.eventAdmin = eventAdmin;
	}
	
	public static IRecentPlaces getRecentPlaces() {
		return places;
	}

	public static void setRecentPlaces(IRecentPlaces p) {
		ServiceManager.places = p;
	}
	
}
