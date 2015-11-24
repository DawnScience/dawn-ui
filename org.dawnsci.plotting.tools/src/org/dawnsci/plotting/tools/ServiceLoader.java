package org.dawnsci.plotting.tools;

import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;

public class ServiceLoader {

	private static IImageFilterService filter;
	private static IImageTransform transformer;
	private static ILoaderService loaderservice;

	public ServiceLoader() {
		
	}

	/**
	 * Injected by OSGI
	 * 
	 * @param it
	 */
	public static void setImageFilter(IImageFilterService ifs) {
		filter = ifs;
	}

	public static IImageFilterService getFilter() {
		return filter;
	}

	/**
	 * Injected by OSGI
	 * 
	 * @param it
	 */
	public static void setImageTransform(IImageTransform it) {
		transformer = it;
	}

	public static IImageTransform getTransformer() {
		return transformer;
	}

	public static void setLoaderService(ILoaderService ls) {
		loaderservice = ls;
	}

	public static ILoaderService getLoaderService() {
		return loaderservice;
	}
}
