package org.dawnsci.osgi.test.application;

import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.IMapPlotController;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSGIInjectionApplication implements IApplication {

	private final static Logger logger = LoggerFactory.getLogger(OSGIInjectionApplication.class);
	

	/**
	 * Must have the path to where the OperationBean is jsoned
	 * as an argument called 'path'
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		logger.debug("Starting OSGI Test Application");
		
		boolean allFound = true;
		
		Class<?>[] services = new Class[] {ILoaderService.class,
				                        IPlottingService.class,
				                        IPersistenceService.class,
				                        IImageService.class,
				                        IPaletteService.class,
				                        IExpressionService.class,
				                        IFileController.class,
				                        IPlotController.class,
				                        IMapFileController.class,
				                        IMapPlotController.class,
				                        IOperationService.class
		};
		
		for (Class<?> c : services) {
			logger.info("Trying " + c.getName());
			Object s = Activator.getService(c);
			
			if (s == null) {
				allFound = false;
				logger.info("Failed for " + c.getName());
			}
		}
		
		if (allFound) {
			logger.info("All services found!");
			return IApplication.EXIT_OK;
		} else {
			logger.info("Failed to find all expected services - product may be broken!");
			return 1;
		}
	}


	@Override
	public void stop() {
		//do nothing
	}

}