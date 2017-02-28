package org.dawnsci.datavis.model;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.osgi.service.event.EventAdmin;

public class ServiceManager {

	private static EventAdmin eventAdmin;
	private static ILoaderService lService;
	private static IFileController fController;
	private static IRecentPlaces rPlaces;
	
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
	
	public static IFileController getFileController() {
		return fController;
	}

	public static void setFileController(IFileController controller) {
		ServiceManager.fController = controller;
	}
	
	public static IRecentPlaces getRecentPlaces() {
		return rPlaces;
	}

	public static void setRecentPlaces(IRecentPlaces places) {
		ServiceManager.rPlaces = places;
	}
	
}
