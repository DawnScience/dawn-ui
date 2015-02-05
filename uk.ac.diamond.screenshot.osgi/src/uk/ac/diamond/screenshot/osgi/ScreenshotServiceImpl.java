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
package uk.ac.diamond.screenshot.osgi;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.screenshot.api.IScreenshotService;
import uk.ac.diamond.screenshot.api.ScreenshotConfiguration;

/**
 * Implementation of IScreenshotService
 * 
 * @author lbq76021
 * 
 */
public class ScreenshotServiceImpl implements IScreenshotService {
	
	private static final Logger logger = LoggerFactory.getLogger(ScreenshotServiceImpl.class);
	
	/**
	 * The file type to use for saving images.
	 */
	private static final int FILE_TYPE = SWT.IMAGE_PNG;
	
	/**
	 * The length of time (in milliseconds) to wait for the UI to be idle before taking
	 * a screenshot anyway.
	 */
	private static final long TIMEOUT_MILLIS = 5000L;
	
	/**
	 * The constructor.
	 */
	public ScreenshotServiceImpl() {
		logger.debug("Constructor called: {}", this.toString());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void takeScreenshot(final ScreenshotConfiguration config) {
		// Check if we are in the UI thread
		if (Display.getCurrent() != null) {
			logger.debug("Already in UI thread - taking screenshot");
			takeScreenshotInUIThread(config);
		} else {
			logger.debug("Not in UI thread - starting synchronous task to take screenshot");
			getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					takeScreenshotInUIThread(config);
				}
			});
		}
	}
	
	/**
	 * Takes a screenshot using the given configuration. Must be run from the UI thread.
	 * 
	 * @param config The screenshot configuration
	 */
	private void takeScreenshotInUIThread(final ScreenshotConfiguration config) {
		Rectangle bounds;
		switch (config.getType()) {
		case ACTIVE_WINDOW:
			bounds = getDisplay().getActiveShell().getBounds();
			break;
		case RECTANGLE:
			bounds = config.getTargetArea();
			if (bounds == null) {
				throw new IllegalArgumentException("A Rectangle must be supplied to use screenshot type RECTANGLE");
			}
			break;
		case WHOLE_DISPLAY:
		default:
			bounds = getDisplay().getBounds();
			break;
		}
		if (!flushUIEventQueue()) {
			logger.info("UI was not idle before taking screenshot {}", config.getFilePath());
		}
		takeRectangleScreenshot(config.getFilePath(), bounds);
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
	
	/**
	 * Gets the Display instance for the workbench.
	 * 
	 * @return the Display
	 */
	private Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}
	
	/**
	 * Sets up an Eclipse Job to save an image to a file, and disposes of the image afterwards
	 * (even if the save operation was unsuccessful).
	 * 
	 * @param image the image to save
	 * @param filePath the path of the file to save the image to
	 * @param fileType the file type as defined by {@link ImageLoader#save(String, int)}
	 * @throws RuntimeException if the file couldn't be saved
	 */
	private void saveAndDisposeImage(final Image image, final String filePath, final int fileType) {
		logger.debug("Setting up job to save image file: {}", filePath);
		final Job job = new Job("Saving image " + filePath) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					final ImageLoader imageLoader = new ImageLoader();
					imageLoader.data = new ImageData[] { image.getImageData() };
					logger.info("Saving image file: {}", filePath);
					imageLoader.save(filePath, fileType);
				} catch (final Exception e) {
					logger.warn("Error saving image file: {}", filePath, e);
					throw new RuntimeException(e);
				} finally {
					image.dispose();
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();
	}
	
	/**
	 * Copies a rectangular area of the display into an image and saves it.
	 * 
	 * @param filePath The path of the image file to save
	 * @param area The area of the display to copy
	 */
	private void takeRectangleScreenshot(final String filePath, final Rectangle area) {
		final Display display = getDisplay();
		final Image image = new Image(display, area);
		final GC displayGC = new GC(display);
		try {
			displayGC.copyArea(image, area.x, area.y);
		} finally {
			displayGC.dispose();
		}
		saveAndDisposeImage(image, filePath, FILE_TYPE);
	}
}
