package org.dawnsci.common.widgets;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;

public class LocalServiceManager {

	private static ILoaderService lservice;
	
	public void setLoaderService(ILoaderService s) {
		lservice = s;
	}

	public static ILoaderService getLoaderService() {
		return lservice;
	}
	
}