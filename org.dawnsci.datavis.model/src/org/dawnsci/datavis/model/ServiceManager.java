package org.dawnsci.datavis.model;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.osgi.service.event.EventAdmin;

public class ServiceManager {

	private static EventAdmin eventAdmin;
	private static ILoaderService lService;
	private static IFileController fController;
	private static IRecentPlaces rPlaces;
	private static IPlottingService plottingService;
	private static IPaletteService paletteService;
	
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

	public static IPlottingService getPlottingService() {
		return plottingService;
	}

	public static void setPlottingService(IPlottingService plottingService) {
		ServiceManager.plottingService = plottingService;
	}
	
	public static IPaletteService getPaletteService() {
		return paletteService;
	}

	public static void setPaletteService(IPaletteService paletteService) {
		ServiceManager.paletteService = paletteService;
	}
	
}
