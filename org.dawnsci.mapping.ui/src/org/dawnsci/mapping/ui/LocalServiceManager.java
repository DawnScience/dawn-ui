package org.dawnsci.mapping.ui;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;

public class LocalServiceManager {

	private static ILoaderService lservice;
	private static IPersistenceService pservice;
	private static IRemoteDatasetService dservice;
	
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

	
}