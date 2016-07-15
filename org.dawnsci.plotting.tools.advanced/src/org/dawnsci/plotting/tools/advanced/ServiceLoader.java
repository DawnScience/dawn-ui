package org.dawnsci.plotting.tools.advanced;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.plotting.api.expressions.IExpressionObjectService;

public class ServiceLoader {

	private static IExpressionObjectService expressiononbjectservice;
	private static IExpressionService expressionservice;

	public ServiceLoader() {
		
	}

	public static IExpressionObjectService getExpressionObjectService() {
		return expressiononbjectservice;
	}

	public static void setExpressionObjectService(IExpressionObjectService eservice) {
		expressiononbjectservice = eservice;
	}

	public static IExpressionService getExpressionService() {
		return expressionservice;
	}

	public static void setExpressionService(IExpressionService expressionservice) {
		ServiceLoader.expressionservice = expressionservice;
	}
}
