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

import org.eclipse.swt.graphics.Rectangle;

/**
 * Immutable configuration object to define screenshot parameters.
 * 
 * @author lbq76021
 *
 */
public class ScreenshotConfiguration {
	
	/**
	 * Screenshot types.
	 */
	public enum ScreenshotType {
		ACTIVE_WINDOW, RECTANGLE, WHOLE_DISPLAY;
	}

	private final String filePath;
	private final ScreenshotType type;
	private final Rectangle area;
	
	/**
	 * Creates a new screenshot configuration.
	 * 
	 * @param filePath The path of the file to save the image to
	 * @param type The screenshot type (must be <code>ACTIVE_WINDOW</code> or <code>WHOLE_DISPLAY</code>)
	 */
	public ScreenshotConfiguration(final String filePath, final ScreenshotType type) {
		if (type == ScreenshotType.RECTANGLE) {
			throw new IllegalArgumentException("A Rectangle must be supplied to use screenshot type RECTANGLE");
		}
		this.filePath = filePath;
		this.type = type;
		this.area = null;
	}

	/**
	 * Creates a new configuration for a rectangular screenshot.
	 * 
	 * @param filePath The path of the file to save the image to
	 * @param type The screenshot type (must be <code>RECTANGLE</code>)
	 * @param area The target area of the screen
	 */
	public ScreenshotConfiguration(final String filePath, final ScreenshotType type, final Rectangle area) {
		// Type should be RECTANGLE - enforce this or silently ignore?
		this.filePath = filePath;
		this.type = type;
		this.area = area;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((area == null) ? 0 : area.hashCode());
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ScreenshotConfiguration other = (ScreenshotConfiguration) obj;
		if (area == null) {
			if (other.area != null)
				return false;
		} else if (!area.equals(other.area))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	/**
	 * Get the file path.
	 * 
	 * @return The file path
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * Get the screenshot type.
	 * 
	 * @return The screenshot type
	 */
	public ScreenshotType getType() {
		return type;
	}
	
	/**
	 * Get the target area.
	 * 
	 * @return The target area
	 */
	public Rectangle getTargetArea() {
		return area;
	}
	
}
