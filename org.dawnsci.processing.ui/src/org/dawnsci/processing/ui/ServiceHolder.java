package org.dawnsci.processing.ui;

import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;

public class ServiceHolder {

	private static IExpressionService expressionService;
	private static EventTracker eventTrackerService;

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
}
