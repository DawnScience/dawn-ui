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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.screenshot.api.IScreenshotService;
import uk.ac.diamond.screenshot.api.ScreenshotConfiguration;
import uk.ac.diamond.screenshot.api.ScreenshotConfiguration.ScreenshotType;

public class UIController {
	
	private static final String ACTIVE_WINDOW_FILE_NAME    = "active_window";
	private static final String ALL_PARTS_DIRECTORY_NAME   = "all_parts";
	private static final String DEFAULT_SAVE_DIR           = System.getProperty("user.home") + "/screenshots/";
	private static final String DIRECTORY_DIALOG_TITLE     = "Choose screenshot directory for this session";
	private static final String EDITOR_FILE_NAME           = "editor:";
	private static final String FILE_BASE_NAME             = "screenshot%03d";
	private static final String FILE_EXTENSION             = ".png";
	private static final String FILE_NAME_PART_SEPARATOR   = "_";
	private static final int    PART_ACTIVATION_RETRIES    = 5;
	private static final String PERSPECTIVE_DIRECTORY_NAME = "perspective:";
	private static final String SPY_SUFFIX                 = "_spy";
	private static final long   TIMEOUT_MILLIS             = 3000;
	private static final String VIEW_FILE_NAME             = "view:";
	private static final String WHOLE_DISPLAY_FILE_NAME    = "whole_display";
	
	private static final Logger logger = LoggerFactory.getLogger(UIController.class);
	
	private static int globalFileCount = 0;
	private static Path baseSaveDir = null;
	
	UIController() {
		logger.debug("Constructor called: {}", this.toString());
	}
	
	void takeScreenshotsOfAllPerspectives() {
		if (checkOrGetSaveDirectory() == null) {
			logger.debug("No target directory supplied, returning silently without taking screenshot");
			return;
		}
		
		// Initialise necessary UI variables
		final IWorkbenchWindow window = getActiveWorkbenchWindow();
		final IWorkbenchPage page = window.getActivePage();
		final IPerspectiveDescriptor originalPerspective = page.getPerspective();
		final IPerspectiveDescriptor[] openPerspectives = page.getSortedPerspectives();
		
		// Loop through open perspectives
		logger.debug("Looping through open perspectives");
		for (final IPerspectiveDescriptor perspective : openPerspectives) {
			// Activate perspective
			logger.debug("Setting perspective '{}' (ID '{}')",
					perspective.getLabel(),
					perspective.getId());
			try {
				page.setPerspective(perspective);
			} catch (final Exception ex) {
				logger.warn("Error trying to set perspective '{}'", perspective.getLabel(), ex);
				continue;
			}
			
			final Path saveDir = baseSaveDir.resolve(PERSPECTIVE_DIRECTORY_NAME + perspective.getLabel());
			try {
				Files.createDirectories(saveDir);
			} catch (final IOException ioException) {
				logger.warn("Could not create directory: {}", saveDir);
				throw new RuntimeException(ioException);
			}
			takeScreenshotsOfCurrentPerspective(saveDir);
		}
		
		// Tidy up
		logger.debug("Finished; setting original perspective '{}' (ID '{}')",
				originalPerspective.getLabel(),
				originalPerspective.getId());
		page.setPerspective(originalPerspective);
	}
	
	void takeScreenshotsOfAllVisibleWorkbenchParts() {
		if (checkOrGetSaveDirectory() == null) {
			logger.debug("No target directory supplied, returning silently without taking screenshot");
			return;
		}
		
		final Path saveDir = baseSaveDir.resolve(ALL_PARTS_DIRECTORY_NAME);
		try {
			Files.createDirectories(saveDir);
		} catch (final IOException e) {
			logger.warn("Could not create directory: {}", saveDir);
			throw new RuntimeException(e);
		}
		
		// takeScreenshotsOfCurrentPerspective currently takes a full set of screenshots of all visible parts,
		// so just call that.
		takeScreenshotsOfCurrentPerspective(saveDir);
	}
	
	void takeWholeDisplayScreenshot() {
		if (checkOrGetSaveDirectory() == null) {
			logger.debug("No target directory supplied, returning silently without taking screenshot");
			return;
		}
		
		final String savePath = buildPathName(baseSaveDir, WHOLE_DISPLAY_FILE_NAME);
		takeWholeDisplayScreenshot(savePath);
	}
	
	void takeWorkbenchWindowScreenshot() {
		if (checkOrGetSaveDirectory() == null) {
			logger.debug("No target directory supplied, returning silently without taking screenshot");
			return;
		}
		
		final String savePath = buildPathName(baseSaveDir, ACTIVE_WINDOW_FILE_NAME);
		takeWorkbenchWindowScreenshot(savePath);
	}
	
	private void activatePluginSpy() {
		logger.debug("Opening Plug-in Spy");
		executeNamedCommand("org.eclipse.pde.runtime.spy.commands.spyCommand");
	}
	
	private String buildFileName(final String namePart) {
		String ret = String.format(FILE_BASE_NAME, ++globalFileCount);
		if (namePart.length() > 0) {
			ret += FILE_NAME_PART_SEPARATOR;
			ret += namePart;
		}
		ret += FILE_EXTENSION;
		return ret;
	}
	
	private String buildPathName(final Path saveDir, final String fileNamePart) {
		final String fileName = buildFileName(fileNamePart);
		return saveDir.resolve(fileName).toString();
	}
	
	private Path checkOrGetSaveDirectory() {
		if (baseSaveDir == null) {
			// Get the target directory from the user
			final DirectoryDialog dialog = new DirectoryDialog(getDisplay().getActiveShell());
			dialog.setText(DIRECTORY_DIALOG_TITLE);
			dialog.setFilterPath(DEFAULT_SAVE_DIR);
			logger.debug(dialog.getFilterPath());
			final String dir = dialog.open();
			
			// If the user selected a directory, update the stored path
			if (dir != null) {
				baseSaveDir = Paths.get(dir);
				logger.debug("Selected save directory: {}", baseSaveDir);
				// Flush UI event queue to ensure dialog is closed and focus is returned to the previous window
				flushUIEventQueue();
			}
		}
		return baseSaveDir;
	}
	
	private void executeNamedCommand(final String commandName) {
		final IHandlerService handlerService =
				(IHandlerService) getActiveWorkbenchWindow().getService(IHandlerService.class);
		try {
			handlerService.executeCommand(commandName, null);
		} catch (final Exception ex) {
			throw new RuntimeException(commandName + " not found");
		}
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
	 * Gets the active workbench window.
	 * 
	 * @return the active workbench window
	 */
	private IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}
	
	/**
	 * Gets the Display instance for the workbench.
	 * 
	 * @return the Display
	 */
	private Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}
	
	private IScreenshotService getScreenshotService() {
		final IScreenshotService screenshotService = ScreenshotServiceTracker.getScreenshotService();
		if (screenshotService == null) {
			logger.warn("No screenshot service is set in ScreenshotServiceTracker - check OSGi status");
			throw new RuntimeException("Screenshot service is not set");
		}
		return screenshotService;
	}
	
	private void takePluginSpyScreenshot(final Path saveDir, final String fileNameSuffix) {
		try {
			activatePluginSpy();
		} catch (final Exception e) {
			logger.warn("Exception trying to open Plug-in Spy");
			return;
		}
		final Shell shell = getDisplay().getActiveShell();
		if (shell.getData().getClass().getSimpleName().equals("SpyDialog")) {
			logger.debug("Plug-in Spy activated, taking screenshot");
			final String savePath = buildPathName(saveDir, fileNameSuffix + SPY_SUFFIX);
			takeRectangleScreenshot(savePath, shell.getBounds());
			shell.close();
		} else {
			logger.debug("Failed to activate Plug-in Spy");
		}
	}
	
	private void takeRectangleScreenshot(final String savePath, final Rectangle bounds) {
		logger.debug("Preparing to take screenshot...");
		final ScreenshotConfiguration screenshotConfiguration =
				new ScreenshotConfiguration(savePath, ScreenshotType.RECTANGLE, bounds);
		getScreenshotService().takeScreenshot(screenshotConfiguration);
	}
	
	/**
	 * Take a screenshot of a workbench part, which must be focussed in the active window.
	 * 
	 * This method tries to identify the part stack which contains the part and take a screenshot of that area alone.
	 * If this is not possible it takes a screenshot of the window containing the part instead.
	 * 
	 * @param savePath The file path to save the image to
	 * @param part The workbench part
	 */
	private void takeScreenshotOfPart(final String savePath, final IWorkbenchPart part) {
		final Rectangle bounds = part.getSite().getShell().getBounds();
		takeRectangleScreenshot(savePath, bounds);
	}
	
	private void takeScreenshotsOfAllVisibleWorkbenchParts(final Path saveDir) {
		logger.debug("Taking screenshots of all currently visible workbench parts");
		final IWorkbenchWindow window = getActiveWorkbenchWindow();
		final IWorkbenchPage page = window.getActivePage();
		final IWorkbenchPartReference originalActivePartReference = page.getActivePartReference();
		
		final IWorkbenchPartReference[] viewReferences = page.getViewReferences();
		takeScreenshotsOfParts(saveDir, page, viewReferences, VIEW_FILE_NAME);
		
		final IWorkbenchPartReference[] editorReferences = page.getEditorReferences();
		takeScreenshotsOfParts(saveDir, page, editorReferences, EDITOR_FILE_NAME);
		
		logger.debug("Finished screenshots; activating originally-active part: {}",
				originalActivePartReference.getPartName());
		page.activate(originalActivePartReference.getPart(true));
	}
	
	private void takeScreenshotsOfCurrentPerspective(final Path saveDir) {
		// Take a screenshots of the whole display and active window for context
		final String wholeDisplayScreenshotSavePath = buildPathName(saveDir, WHOLE_DISPLAY_FILE_NAME);
		takeWholeDisplayScreenshot(wholeDisplayScreenshotSavePath);
		
		final String activeWindowScreenshotSavePath = buildPathName(saveDir, ACTIVE_WINDOW_FILE_NAME);
		takeWorkbenchWindowScreenshot(activeWindowScreenshotSavePath);
		
		// Then take screenshots of all parts visible in the current perspective
		takeScreenshotsOfAllVisibleWorkbenchParts(saveDir);
	}
	
	private void takeScreenshotsOfParts(
			final Path saveDir,
			final IWorkbenchPage page,
			final IWorkbenchPartReference[] partReferences,
			final String category) {
		for (final IWorkbenchPartReference partReference : partReferences) {
			final IWorkbenchPart part = partReference.getPart(true);
			if (part != null) {
				// Activate part and take a screenshot
				logger.debug("Activating {} {}", category, partReference.getPartName());
				for (int ii = 0; ii < PART_ACTIVATION_RETRIES; ii++) {
					try {
						page.activate(part);
						flushUIEventQueue();
					} catch (final Exception ex) {
						logger.warn("Error trying to activate part: {}", partReference.getPartName(), ex);
					}
					final IWorkbenchPart activePart = page.getActivePart();
					if (part == activePart) {
						logger.debug("Activated {} {}", category, partReference.getPartName());
						break;
					} else {
						logger.warn("Failed to activate part: {}, currently active part is: {}", part, activePart);
					}
				}
				
				final String fileNameSuffix = category + partReference.getPartName();
				final String savePath = buildPathName(saveDir, fileNameSuffix);
				takeScreenshotOfPart(savePath, part);
				takePluginSpyScreenshot(saveDir, fileNameSuffix);
				
			} else {
				logger.debug("Could not get workbench part for {} {}", category, partReference.getPartName());
			}
		}
	}
	
	private void takeWholeDisplayScreenshot(final String savePath) {
		logger.debug("Preparing to take screenshot of whole display...");
		final ScreenshotConfiguration screenshotConfiguration =
				new ScreenshotConfiguration(savePath, ScreenshotType.WHOLE_DISPLAY);
		getScreenshotService().takeScreenshot(screenshotConfiguration);
	}
	
	private void takeWorkbenchWindowScreenshot(final String savePath) {
		final Rectangle shellBounds = getActiveWorkbenchWindow().getShell().getBounds();
		takeRectangleScreenshot(savePath, shellBounds);
	}
}
