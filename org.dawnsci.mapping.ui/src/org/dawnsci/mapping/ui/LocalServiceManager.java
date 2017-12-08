package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.osgi.service.event.EventAdmin;

public class LocalServiceManager {

	private static ILoaderService lservice;
	private static IPersistenceService pservice;
	private static IRemoteDatasetService dservice;
	private static INexusFileFactory nexusFactory;
	private static EventAdmin eventAdmin;
	private static IMarshallerService marshallerService;
	private static IMapFileController fileController;

	public static void setLoaderService(ILoaderService s) {
		lservice = s;
	}

	public static ILoaderService getLoaderService() {
		return lservice;
	}
	
	public static void setPersistenceService(IPersistenceService s) {
		pservice = s;
	}
	
	public static IPersistenceService getPersistenceService() {
		return pservice;
	}

	public static IRemoteDatasetService getRemoteDatasetService() {
		return dservice;
	}

	public static void setRemoteDatasetService(IRemoteDatasetService d) {
		dservice = d;
	}
	
	public static INexusFileFactory getNexusFactory() {
		return nexusFactory;
	}

	public static void setNexusFactory(INexusFileFactory nexusFactory) {
		LocalServiceManager.nexusFactory = nexusFactory;
	}
	
	public static EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	public static void setEventAdmin(EventAdmin eventAdmin) {
		LocalServiceManager.eventAdmin = eventAdmin;
	}

	public static IMarshallerService getMarshallerService() {
		return marshallerService;
	}

	public static void setMarshallerService(IMarshallerService marshallerService) {
		LocalServiceManager.marshallerService = marshallerService;
	}
	
	public static IMapFileController getFileController() {
		return fileController;
	}

	public static void setFileController(IMapFileController fileController) {
		LocalServiceManager.fileController = fileController;
	}
	
}