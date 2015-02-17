/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Colin Palmer - initial API, implementation and documentation
 */
package uk.ac.diamond.screenshot.uisurvey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.screenshot.api.IScreenshotService;

/**
 * Class to hold a reference to the current implementation of IScreenshotService. This class is
 * only <code>public</code> to allow access by Equinox DS.
 * 
 * @author lbq76021
 *
 */
public class ScreenshotServiceTracker {
	
	private final Logger logger = LoggerFactory.getLogger(ScreenshotServiceTracker.class);
	
	private static IScreenshotService screenshotService;
	
	public ScreenshotServiceTracker() {
		logger.debug("Constructor called: {}", this.toString());
	}
	
	/**
	 * @return The current IScreenshotService, or <code>null</code>
	 */
	static IScreenshotService getScreenshotService() {
		return screenshotService;
	}
	
	public synchronized void setScreenshotService(final IScreenshotService service) {
		logger.debug("Screenshot service set by call to: {}", this.toString());
		screenshotService = service;
	}
	
	public synchronized void unsetScreenshotService(final IScreenshotService service) {
		logger.debug("Screenshot service unset by call to: {}", this.toString());
		if (screenshotService == service) {
			screenshotService = null;
		}
	}
}
