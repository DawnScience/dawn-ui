package org.dawnsci.plotting.draw2d.swtxy.util;

import org.eclipse.draw2d.geometry.Point;

/**
 * A parametric point function that generates a 2D point from given parameters
 */
public interface PointFunction {

	/**
	 * Calculate a point from given parameter values
	 * @param parameters
	 * @return
	 */
	public Point calculatePoint(double... parameter);
}
