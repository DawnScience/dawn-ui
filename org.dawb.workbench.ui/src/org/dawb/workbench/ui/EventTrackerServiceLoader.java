package org.dawb.workbench.ui;

import org.eclipse.dawnsci.analysis.api.EventTracker;

/**
 * This class is used to inject the analytics service using OSGI and retrieve in ConvertWizard
 * @author wqk87977
 *
 */
public class EventTrackerServiceLoader {

	private static EventTracker service;

	/**
	 * Used for OSGI injection
	 */
	public EventTrackerServiceLoader() {
		
	}

	/**
	 * Injected by OSGI
	 * @param at
	 */
	public static void setService(EventTracker et) {
		service = et;
	}

	public static EventTracker getService() {
		return service;
	}

}