package org.dawnsci.datavis.live;

import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.scanning.api.event.IEventService;

public class ServiceManager {
	
	private static IRemoteDatasetService dservice;
	private static IEventService eventService;

	public static IRemoteDatasetService getRemoteDatasetService() {
		return dservice;
	}

	public static void setRemoteDatasetService(IRemoteDatasetService d) {
		dservice = d;
	}
	
	public static void setIEventService(IEventService service) {
		eventService = service;
	}

	public static IEventService getIEventService() {
		return eventService;
	}
}

