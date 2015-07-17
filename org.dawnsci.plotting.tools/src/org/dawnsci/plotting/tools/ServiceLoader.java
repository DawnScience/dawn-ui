package org.dawnsci.plotting.tools;

import org.eclipse.dawnsci.analysis.api.image.IImageTransform;

public class ServiceLoader {

	private static IImageTransform transformer;

	public ServiceLoader() {
		
	}

	/**
	 * Injected by OSGI
	 * 
	 * @param it
	 */
	public static void setImageTransform(IImageTransform it) {
		transformer = it;
	}

	public static IImageTransform getImageTransform() {
		return transformer;
	}

}
