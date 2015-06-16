package org.dawnsci.plotting.tools;

import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;

public class ImageFilterServiceLoader {

	private static IImageFilterService filter;

	public ImageFilterServiceLoader() {
		
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
}
