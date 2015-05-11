package org.dawnsci.processing.ui;

import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;

public class ServiceHolder {

	private static IExpressionService expressionService;
	private static EventTracker trackerService;

	public static IExpressionService getExpressionService() {
		return expressionService;
	}

	public static void setExpressionService(IExpressionService expressionService) {
		ServiceHolder.expressionService = expressionService;
	}

	public static EventTracker getTrackerService() {
		return trackerService;
	}

	public static void setTrackerService(EventTracker trackerService) {
		ServiceHolder.trackerService = trackerService;
	}

}
