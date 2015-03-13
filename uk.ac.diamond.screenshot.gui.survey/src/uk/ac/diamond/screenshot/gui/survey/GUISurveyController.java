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
package uk.ac.diamond.screenshot.gui.survey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.screenshot.api.IScreenshotService;
import uk.ac.diamond.screenshot.api.ScreenshotConfiguration;
import uk.ac.diamond.screenshot.api.ScreenshotConfiguration.ScreenshotType;
import uk.ac.diamond.screenshot.gui.FileNumberTracker;
import uk.ac.diamond.screenshot.gui.GuiUtils;
import uk.ac.diamond.screenshot.gui.SaveDirectoryTracker;
import uk.ac.diamond.screenshot.gui.ScreenshotController;

public class GUISurveyController {
	
	private static final String ACTIVE_WINDOW_FILE_NAME    = "active_window";
	private static final String ALL_PARTS_DIRECTORY_NAME   = "all_parts";
	private static final String EDITOR_FILE_NAME           = "editor-";
	private static final String FILE_BASE_NAME             = "screenshot%03d";
	private static final String FILE_EXTENSION             = ".png";
	private static final String FILE_NAME_PART_SEPARATOR   = "_";
	private static final int    PART_ACTIVATION_RETRIES    = 3;
	private static final String PERSPECTIVE_DIRECTORY_NAME = "perspective-";
	private static final String SPY_SUFFIX                 = "_spy";
	private static final String VIEW_FILE_NAME             = "view-";
	private static final String WHOLE_DISPLAY_FILE_NAME    = "whole_display";
	
	private static final Logger logger = LoggerFactory.getLogger(GUISurveyController.class);
	
	public GUISurveyController() {
		logger.trace("Constructor called: {}", this);
	}
	
	public void takeScreenshotsOfAllPerspectives() {
		if (getBaseSaveDir() == null) {
			logger.debug("No save directory supplied, returning silently without taking screenshot");
			return;
		}
		
		// Initialise necessary UI variables
		final IWorkbenchWindow window = GuiUtils.getActiveWorkbenchWindow();
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
			
			final Path saveDir = createSubDirectory(getBaseSaveDir(), PERSPECTIVE_DIRECTORY_NAME + perspective.getLabel());
			takeScreenshotsOfCurrentPerspective(saveDir);
		}
		
		// Tidy up
		logger.debug("Finished; setting original perspective '{}' (ID '{}')",
				originalPerspective.getLabel(),
				originalPerspective.getId());
		page.setPerspective(originalPerspective);
	}
	
	public void takeScreenshotsOfAllVisibleWorkbenchParts() {
		if (getBaseSaveDir() == null) {
			logger.debug("No target directory supplied, returning silently without taking screenshot");
			return;
		}
		
		final Path saveDir = createSubDirectory(getBaseSaveDir(), ALL_PARTS_DIRECTORY_NAME);
		
		// takeScreenshotsOfCurrentPerspective currently takes a full set of screenshots of all visible parts,
		// so just call that.
		takeScreenshotsOfCurrentPerspective(saveDir);
	}
	
	private Path getBaseSaveDir() {
		return SaveDirectoryTracker.checkOrGetSaveDirectory();
	}
	
	private Path createSubDirectory(Path baseDir, String subDirName) {
		Path subDir = baseDir.resolve(subDirName);
		try {
			Files.createDirectories(subDir);
		} catch (final IOException ioException) {
			logger.warn("Could not create directory: {}", subDir);
			throw new RuntimeException(ioException);
		}
		return subDir;
	}
	
	private void takeScreenshotsOfCurrentPerspective(final Path saveDir) {
		// Take a screenshots of the whole display and active window for context
		final String wholeDisplayScreenshotSavePath = buildPathName(saveDir, WHOLE_DISPLAY_FILE_NAME);
		takeWholeDisplayScreenshot(wholeDisplayScreenshotSavePath);
		
		final String activeWindowScreenshotSavePath = buildPathName(saveDir, ACTIVE_WINDOW_FILE_NAME);
		takeWorkbenchWindowScreenshot(activeWindowScreenshotSavePath);
		
		// Then take screenshots of all parts visible in the current perspective
		takeScreenshotsOfAllParts(saveDir);
	}
	
	private String buildPathName(final Path saveDir, final String fileNamePart) {
		final String fileName = buildFileName(fileNamePart);
		return saveDir.resolve(fileName).toString();
	}
	
	private String buildFileName(final String namePart) {
		String ret = String.format(FILE_BASE_NAME, FileNumberTracker.getNextFileNumber());
		if (namePart.length() > 0) {
			ret += FILE_NAME_PART_SEPARATOR;
			ret += namePart;
		}
		ret += FILE_EXTENSION;
		return ret;
	}
	
	private void takeWholeDisplayScreenshot(final String savePath) {
		logger.debug("Preparing to take screenshot of whole display...");
		final ScreenshotConfiguration screenshotConfiguration =
				new ScreenshotConfiguration(savePath, ScreenshotType.WHOLE_DISPLAY);
		ScreenshotController.getScreenshotService().takeScreenshot(screenshotConfiguration);
	}
	
	private void takeWorkbenchWindowScreenshot(final String savePath) {
		logger.debug("Preparing to take screenshot of current window...");
		final Rectangle shellBounds = GuiUtils.getActiveWorkbenchWindow().getShell().getBounds();
		takeRectangleScreenshot(savePath, shellBounds);
	}
	
	private void takeScreenshotsOfAllParts(final Path saveDir) {
		logger.debug("Taking screenshots of all currently visible workbench parts");
		final IWorkbenchWindow window = GuiUtils.getActiveWorkbenchWindow();
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
						GuiUtils.flushUIEventQueue();
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
	
	private void takeScreenshotOfPart(final String savePath, final IWorkbenchPart part) {
		logger.debug("Preparing to take screenshot of part: {}", part.getTitle());
		final Rectangle bounds = part.getSite().getShell().getBounds();
		takeRectangleScreenshot(savePath, bounds);
	}
	
	private void takeRectangleScreenshot(final String savePath, final Rectangle bounds) {
		final ScreenshotConfiguration screenshotConfiguration =
				new ScreenshotConfiguration(savePath, ScreenshotType.RECTANGLE, bounds);

		final IScreenshotService screenshotService = ScreenshotController.getScreenshotService();
		if (screenshotService != null) {
			screenshotService.takeScreenshot(screenshotConfiguration);
		} else {
			logger.warn("Screenshot service is not set - no screenshot was taken");
		}
	}
	
	private void takePluginSpyScreenshot(final Path saveDir, final String fileNameSuffix) {
		try {
			activatePluginSpy();
		} catch (final Exception e) {
			logger.warn("Exception trying to open Plug-in Spy");
			return;
		}
		final Shell shell = GuiUtils.getDisplay().getActiveShell();
		if (shell.getData().getClass().getSimpleName().equals("SpyDialog")) {
			logger.debug("Plug-in Spy activated, taking screenshot");
			final String savePath = buildPathName(saveDir, fileNameSuffix + SPY_SUFFIX);
			takeRectangleScreenshot(savePath, shell.getBounds());
			shell.close();
		} else {
			logger.debug("Failed to activate Plug-in Spy");
		}
	}
	
	private void activatePluginSpy() {
		logger.debug("Opening Plug-in Spy");
		executeNamedCommand("org.eclipse.pde.runtime.spy.commands.spyCommand");
	}
	
	private void executeNamedCommand(final String commandName) {
		final IHandlerService handlerService =
				(IHandlerService) GuiUtils.getActiveWorkbenchWindow().getService(IHandlerService.class);
		try {
			handlerService.executeCommand(commandName, null);
		} catch (final Exception ex) {
			throw new RuntimeException(commandName + " not found");
		}
	}
}
