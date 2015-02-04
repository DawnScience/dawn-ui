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
package uk.ac.diamond.screenshot.api;


/**
 * Implementations of this service provide screenshot methods to Eclipse applications.
 * 
 * @author lbq76021
 * 
 */
public interface IScreenshotService {
	
	/**
	 * Takes a screenshot.
	 * 
	 * @param config The screenshot configuration to use
	 */
	public void takeScreenshot(ScreenshotConfiguration config);
	
}
