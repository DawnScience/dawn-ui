package org.dawnsci.datavis;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.IPlotController;
import org.osgi.service.event.EventAdmin;

public class ServiceManager {

	private static EventAdmin eventAdmin;
	private static IRecentPlaces places;
	private static IPlotController plotController;
	
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

	public static IPlotController getPlotController() {
		return plotController;
	}

	public static void setPlotController(IPlotController plotController) {
		ServiceManager.plotController = plotController;
	}
	
}
