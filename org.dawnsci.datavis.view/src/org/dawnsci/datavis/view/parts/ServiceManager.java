package org.dawnsci.datavis.view.parts;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.IFileController;

public class ServiceManager {
	
	private static IFileController fController;
	private static IRecentPlaces places;
	
	public static IFileController getFileController() {
		return fController;
	}

	public static void setFileController(IFileController controller) {
		ServiceManager.fController = controller;
	}
	
	public static IRecentPlaces getRecentPlaces() {
		return places;
	}

	public static void setRecentPlaces(IRecentPlaces p) {
		ServiceManager.places = p;
	}
	
	
}
