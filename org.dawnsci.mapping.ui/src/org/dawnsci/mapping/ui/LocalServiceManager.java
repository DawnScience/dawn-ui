package org.dawnsci.mapping.ui;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;

public class LocalServiceManager {

	private static ILoaderService lservice;
	
	public static void setLoaderService(ILoaderService s) {
		lservice = s;
	}
	
	public static ILoaderService getLoaderService() {
		return lservice;
	}
	
}