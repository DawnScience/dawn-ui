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
package uk.ac.diamond.screenshot.gui;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.screenshot.api.IScreenshotService;
import uk.ac.diamond.screenshot.api.ScreenshotConfiguration;
import uk.ac.diamond.screenshot.api.ScreenshotConfiguration.ScreenshotType;

public class ScreenshotController {

	private static String FILE_NAME = "screenshot%03d.png";

	private final Logger logger = LoggerFactory.getLogger(ScreenshotController.class);

	public ScreenshotController() {
		logger.trace("Constructor called: {}", this);
	}

	public void takeActiveWindowScreenshot() {
		takeScreenshot(ScreenshotType.ACTIVE_WINDOW);
	}

	public void takeWholeDisplayScreenshot() {
		takeScreenshot(ScreenshotType.WHOLE_DISPLAY);
	}

	private void takeScreenshot(final ScreenshotType type) {
		logger.debug("Preparing to take screenshot...");
		final Path saveDir = SaveDirectoryTracker.checkOrGetSaveDirectory();
		if (saveDir == null) {
			logger.debug("No save directory supplied, returning silently without taking screenshot");
			return;
		}

		int fileNum = FileNumberTracker.getNextFileNumber();
		Path savePath = saveDir.resolve(String.format(FILE_NAME, fileNum));

		final ScreenshotConfiguration screenshotConfiguration = new ScreenshotConfiguration(savePath.toString(), type);

		final IScreenshotService screenshotService = getScreenshotService();
		if (screenshotService != null) {
			screenshotService.takeScreenshot(screenshotConfiguration);
		} else {
			logger.warn("Screenshot service is not set - no screenshot was taken");
		}
	}

	public static IScreenshotService getScreenshotService() {
		return ScreenshotServiceTracker.getScreenshotService();
	}
}
