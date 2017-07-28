package org.dawnsci.plotting.tools;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.plotting.api.expressions.IExpressionObjectService;

public class ServiceLoader {

	private static IImageFilterService filter;
	private static IImageTransform transformer;
	private static ILoaderService loaderservice;
	private static IExpressionObjectService expressiononbjectservice;
	private static IConversionService conversionservice;
	private static IPersistenceService persistenceService;

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

	public static IExpressionObjectService getExpressionObjectService() {
		return expressiononbjectservice;
	}

	public static void setExpressionObjectService(IExpressionObjectService eservice) {
		expressiononbjectservice = eservice;
	}

	public static IConversionService getConversionService() {
		return conversionservice;
	}

	public static void setConversionService(IConversionService cservice) {
		conversionservice = cservice;
	}
	
	public static IPersistenceService getPersistenceService() {
		return persistenceService;
	}

	public static void setPersistenceService(IPersistenceService pservice) {
		persistenceService = pservice;
	}
}
