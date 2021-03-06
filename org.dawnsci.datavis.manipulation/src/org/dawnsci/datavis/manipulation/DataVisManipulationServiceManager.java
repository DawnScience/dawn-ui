package org.dawnsci.datavis.manipulation;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.IFileController;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.osgi.service.event.EventAdmin;

public class DataVisManipulationServiceManager {
	private static EventAdmin eventAdmin;
	private static IExpressionService expressionService;
	private static IFileController fileController;
	private static IRecentPlaces recentPlaces;
	private static INexusFileFactory nexusFileFactory;
	private static IPlottingService plottingService;
	private static ILoaderService loaderService;

	public void setEventAdmin(EventAdmin admin) {
		eventAdmin = admin;
	}

	public static EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	public void setExpressionService(IExpressionService service) {
		expressionService = service;
	}

	public static IExpressionService getExpressionService() {
		return expressionService;
	}

	public void setFileController(IFileController controller) {
		fileController = controller;
	}

	public static IFileController getFileController() {
		return fileController;
	}

	public void setLoaderService(ILoaderService service) {
		loaderService = service;
	}

	public static ILoaderService getLoaderService() {
		return loaderService;
	}

	public void setNexusFileFactory(INexusFileFactory factory) {
		nexusFileFactory = factory;
	}

	public static INexusFileFactory getNexusFileFactory() {
		return nexusFileFactory;
	}

	public void setPlottingService(IPlottingService service) {
		plottingService = service;
	}

	public static IPlottingService getPlottingService() {
		return plottingService;
	}

	public void setRecentPlaces(IRecentPlaces places) {
		recentPlaces = places;
	}

	public static IRecentPlaces getRecentPlaces() {
		return recentPlaces;
	}
}
