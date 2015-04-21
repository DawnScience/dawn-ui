package org.dawnsci.processing.ui;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;

public class ServiceHolder {

	private static IExpressionService expressionService;

	public static IExpressionService getExpressionService() {
		return expressionService;
	}

	public static void setExpressionService(IExpressionService expressionService) {
		ServiceHolder.expressionService = expressionService;
	}

}
