package org.dawnsci.plotting.tools;

import org.dawnsci.processing.ui.api.IOperationUIService;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.plotting.api.expressions.IExpressionObjectService;
import org.osgi.service.event.EventAdmin;

public class ServiceLoader {

	private static IImageFilterService filter;
	private static IImageTransform transformer;
	private static ILoaderService loaderservice;
	private static IExpressionObjectService expressiononbjectservice;
	private static IConversionService conversionservice;
	private static IPersistenceService persistenceService;
	private static INexusFileFactory nexusFileFactory;
	private static EventAdmin eventAdmin;
	private static IOperationService operationService;
	private static IOperationUIService operationUIService;

	public ServiceLoader() {
		
	}

	/**
	 * Injected by OSGI
	 * 
	 * @param it
	 */
	public void setImageFilter(IImageFilterService ifs) {
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
	public void setImageTransform(IImageTransform it) {
		transformer = it;
	}

	public static IImageTransform getTransformer() {
		return transformer;
	}

	public void setLoaderService(ILoaderService ls) {
		loaderservice = ls;
	}

	public static ILoaderService getLoaderService() {
		return loaderservice;
	}

	public static IExpressionObjectService getExpressionObjectService() {
		return expressiononbjectservice;
	}

	public void setExpressionObjectService(IExpressionObjectService eservice) {
		expressiononbjectservice = eservice;
	}

	public static IConversionService getConversionService() {
		return conversionservice;
	}

	public void setConversionService(IConversionService cservice) {
		conversionservice = cservice;
	}
	
	public static IPersistenceService getPersistenceService() {
		return persistenceService;
	}

	public void setPersistenceService(IPersistenceService pservice) {
		persistenceService = pservice;
	}

	public static INexusFileFactory getNexusFileFactory() {
		return nexusFileFactory;
	}

	public void setNexusFileFactory(INexusFileFactory nexusFileFactory) {
		ServiceLoader.nexusFileFactory = nexusFileFactory;
	}
	
	public static EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(EventAdmin eventAdmin) {
		ServiceLoader.eventAdmin = eventAdmin;
	}
	
	public static IOperationUIService getOperationUIService() {
		return operationUIService;
	}

	public void setOperationUIService(IOperationUIService operationUIService) {
		ServiceLoader.operationUIService = operationUIService;
	}
	
	public static IOperationService getOperationService() {
		return operationService;
	}

	public void setOperationService(IOperationService operationService) {
		ServiceLoader.operationService = operationService;
	}

}
