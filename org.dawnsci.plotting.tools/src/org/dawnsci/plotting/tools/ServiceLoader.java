package org.dawnsci.plotting.tools;

import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;

public class ServiceLoader {

	private static IImageFilterService filter;
	private static IImageTransform transformer;

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
}
