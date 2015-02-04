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
package uk.ac.diamond.screenshot.simplegui;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.screenshot.api.IScreenshotService;
import uk.ac.diamond.screenshot.api.ScreenshotConfiguration;
import uk.ac.diamond.screenshot.api.ScreenshotConfiguration.ScreenshotType;

public class ScreenshotCommandHandler extends AbstractHandler {
	
	private static final String DIRECTORY_DIALOG_TITLE = "Choose screenshot directory for this session";
	private static final String DEFAULT_SAVE_DIR = System.getProperty("user.home") + "/screenshots/";
	private static final long TIMEOUT_MILLIS = 3000;
	
	private static int globalFileCount = 0;
	private static String fileName = "screenshot_%03d.png";
	private static File saveDir = null;
	
	private final Logger logger = LoggerFactory.getLogger(ScreenshotCommandHandler.class);
	
	public ScreenshotCommandHandler() {
		logger.debug("Constructor called: {}", this.toString());
	}
	
	@Override
	public Object execute(final ExecutionEvent event) {
		logger.debug("Single screenshot command executing");
		if (checkOrGetSaveDirectory() != null) {
			takeScreenshot();
		} else {
			logger.debug("No target directory supplied, returning silently without taking screenshot");
		}
		return null;
	}
	
	private IScreenshotService getScreenshotService() {
		final IScreenshotService screenshotService = ScreenshotServiceTracker.getScreenshotService();
		if (screenshotService == null) {
			logger.debug("No screenshot service is set in ScreenshotServiceTracker - check OSGi status");
			throw new RuntimeException("Screenshot service is not set");
		}
		return screenshotService;
	}
	
	private void takeScreenshot() {
		logger.debug("Preparing to take screenshot...");
		
		// Increment file count until we find a name which doesn't exist
		// (Should really do this outside the UI thread...)
		File savePath;
		final long endTime = System.currentTimeMillis() + TIMEOUT_MILLIS;
		do {
			savePath = new File(saveDir, String.format(fileName, ++globalFileCount));
			if (System.currentTimeMillis() > endTime) {
				logger.warn("Timed out trying to find unused filename");
				return;
			}
		} while (savePath.exists());
		
		// Create a ScreenshotConfiguration and take a screenshot
		final ScreenshotConfiguration screenshotConfiguration =
				new ScreenshotConfiguration(savePath.getPath(), ScreenshotType.ACTIVE_WINDOW);
		getScreenshotService().takeScreenshot(screenshotConfiguration);
	}

	private File checkOrGetSaveDirectory() {
		if (saveDir == null) {
			// Get the target directory from the user
			final DirectoryDialog dialog = new DirectoryDialog(getDisplay().getActiveShell());
			dialog.setText(DIRECTORY_DIALOG_TITLE);
			dialog.setFilterPath(DEFAULT_SAVE_DIR);
			logger.debug(dialog.getFilterPath());
			final String dir = dialog.open();
			
			// If the user selected a directory, update the stored path
			if (dir != null) {
				saveDir = new File(dir);
				logger.debug("Selected save directory: {}", saveDir);
				// Ensure the selected directory exists
				saveDir.mkdirs();
				// Flush UI event queue to ensure dialog is closed and focus is returned to the previous window
				flushUIEventQueue();
			}
		}
		return saveDir;
	}
	
	/**
	 * Gets the Display instance for the workbench.
	 * 
	 * @return the Display
	 */
	private Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}

	/**
	 * Waits for the UI to be idle. If this method is called and returns <code>true</code> before
	 * a screenshot is taken, then any recent changes to the UI should have been fully repainted
	 * and will be included in the screenshot.
	 * 
	 * This implementation calls Display#readAndDispatch repeatedly to clear the UI event queue.
	 * Must be called from the UI thread.
	 * 
	 * @return <code>true</code> if the queue was successfully emptied, <code>false</code> if the
	 * operation timed out
	 */
	private boolean flushUIEventQueue() {
		logger.debug("Flushing UI event queue...");
		final Display display = getDisplay();
		int count = 0;
		final long endTime = System.currentTimeMillis() + TIMEOUT_MILLIS;
		boolean result = true;
		while (display.readAndDispatch()) {
			count++;
			if (System.currentTimeMillis() > endTime) {
				logger.debug("Timed out waiting for UI event queue to be empty");
				result = false;
				break;
			}
		}
		logger.debug("Dispatched {} UI events", count);
		return result;
	}
}
