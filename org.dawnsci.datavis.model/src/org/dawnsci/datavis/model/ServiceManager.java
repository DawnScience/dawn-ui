package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.osgi.service.event.EventAdmin;

public class ServiceManager {

	private static EventAdmin eventAdmin;
	private static ILoaderService lService;
	
	public static ILoaderService getLoaderService() {
		return lService;
	}

	public static void setLoaderService(ILoaderService lService) {
		ServiceManager.lService = lService;
	}

	public static EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	public static void setEventAdmin(EventAdmin eventAdmin) {
		ServiceManager.eventAdmin = eventAdmin;
	}
	
}
