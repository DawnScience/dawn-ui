package org.dawnsci.plotting.draw2d.swtxy.util;

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.geometry.Point;

/**
 * A parametric point function that generates a 2D point from given parameters
 */
public interface PointFunction {

	/**
	 * 
	 * @param system
	 */
	public void setCoordinateSystem(ICoordinateSystem system);

	/**
	 * Calculate a point from given parameter values
	 * @param parameters
	 * @return
	 */
	public Point calculatePoint(double... parameter);

	/**
	 * Calculate parameters which will make point intersect given vertical line of given x
	 * @param x
	 * @return an array of doubles (can null or contain one or two values)
	 */
	public double[] calculateXIntersectionParameters(int x);

	/**
	 * Calculate parameters which will make point intersect given horizontal line of given y
	 * @param y
	 * @return an array of doubles (can null or contain one or two values)
	 */
	public double[] calculateYIntersectionParameters(int y);
}
