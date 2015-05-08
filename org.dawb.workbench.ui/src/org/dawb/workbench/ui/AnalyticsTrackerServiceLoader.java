package org.dawb.workbench.ui;

import org.eclipse.dawnsci.analysis.api.IAnalyticsTracker;

/**
 * This class is used to inject the analytics service using OSGI and retrieve in ConvertWizard
 * @author wqk87977
 *
 */
public class AnalyticsTrackerServiceLoader {

	private static IAnalyticsTracker service;

	/**
	 * Used for OSGI injection
	 */
	public AnalyticsTrackerServiceLoader() {
		
	}

	/**
	 * Injected by OSGI
	 * @param at
	 */
	public static void setService(IAnalyticsTracker at) {
		service = at;
	}

	public static IAnalyticsTracker getService() {
		return service;
	}

}