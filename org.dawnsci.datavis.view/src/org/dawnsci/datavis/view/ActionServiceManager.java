package org.dawnsci.datavis.view;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.IFileController;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.osgi.service.event.EventAdmin;

public class ActionServiceManager {
	private static EventAdmin eventAdmin;
	private static IExpressionService expressionService;
	private static IFileController fileController;
	private static IRecentPlaces recentPlaces;
	private static INexusFileFactory nexusFileFactory;

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

	public void setNexusFileFactory(INexusFileFactory factory) {
		nexusFileFactory = factory;
	}

	public static INexusFileFactory getNexusFileFactory() {
		return nexusFileFactory;
	}

	public void setRecentPlaces(IRecentPlaces places) {
		recentPlaces = places;
	}

	public static IRecentPlaces getRecentPlaces() {
		return recentPlaces;
	}
}
