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
package uk.ac.diamond.screenshot.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.screenshot.gui.ScreenshotController;

public class SingleWholeDisplayScreenshotCommandHandler extends AbstractHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SingleWholeDisplayScreenshotCommandHandler.class);
	
	/**
	 * The constructor.
	 */
	public SingleWholeDisplayScreenshotCommandHandler() {
		// empty
	}
	
	@Override
	public Object execute(final ExecutionEvent event) {
		logger.debug("Single Whole Display screenshot command executing");
		new ScreenshotController().takeWholeDisplayScreenshot();
		return null;
	}
}
