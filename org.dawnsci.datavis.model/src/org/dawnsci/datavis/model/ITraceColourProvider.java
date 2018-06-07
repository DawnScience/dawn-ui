package org.dawnsci.datavis.model;

import org.eclipse.swt.graphics.RGB;

/**
 * Interface for setting the line trace color scheme
 * in the DataVis plot controller
 *
 */
public interface ITraceColourProvider {

	/**
	 * Get the name of the color provider
	 * @return name
	 */
	String getName();
	
	/**
	 * Get the array of RGB values used to color
	 * the traces
	 * @return rgbs
	 */
	RGB[] getRGBs();
	
}
