package org.dawnsci.processing.ui;

import org.dawnsci.processing.ui.api.IOperationUIService;
import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationExporterService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.osgi.service.event.EventAdmin;

public class ServiceHolder {

	private static IExpressionService expressionService;
	private static EventTracker eventTrackerService;
	private static IConversionService conversionService;
	private static IOperationService operationService;
	private static ILoaderService loaderService;
	private static IOperationExporterService exporterService;
	private static EventAdmin eventAdmin;
	private static IPersistenceService persistenceService;
	private static IOperationUIService operationUIService;

	public static IExpressionService getExpressionService() {
		return expressionService;
	}

	public static void setExpressionService(IExpressionService expressionService) {
		ServiceHolder.expressionService = expressionService;
	}

	public static EventTracker getEventTrackerService() {
		return eventTrackerService;
	}

	public static void setEventTrackerService(EventTracker eventTrackerService) {
		ServiceHolder.eventTrackerService = eventTrackerService;
	}

	public static void setConversionService(IConversionService s) {
		conversionService = s;
	}

	public static IConversionService getConversionService() {
		return conversionService;
	}

	public static void setOperationService(IOperationService s) {
		operationService = s;
	}

	public static IOperationService getOperationService() {
		return operationService;
	}

	public static void setLoaderService(ILoaderService s) {
		loaderService = s;
	}

	public static ILoaderService getLoaderService() {
		return loaderService;
	}

	public static void setOperationExporter(IOperationExporterService s) {
		exporterService = s;
	}

	public static IOperationExporterService getOperationExporter() {
		return exporterService;
	}

	public static EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	public static void setEventAdmin(EventAdmin eventAdmin) {
		ServiceHolder.eventAdmin = eventAdmin;
	}

	public static IPersistenceService getPersistenceService() {
		return persistenceService;
	}

	public static void setPersistenceService(IPersistenceService persistenceService) {
		ServiceHolder.persistenceService = persistenceService;
	}
	
	public static IOperationUIService getOperationUIService() {
		return operationUIService;
	}
	
	public static void setOperationUIService(IOperationUIService operationUIService) {
		ServiceHolder.operationUIService = operationUIService;
	}
}
