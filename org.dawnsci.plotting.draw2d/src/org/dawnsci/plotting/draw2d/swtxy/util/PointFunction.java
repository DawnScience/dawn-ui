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
	 * @return aspect ratio of coordinate system
	 */
	public double getAspectRatio();

	/**
	 * Calculate a point from given parameter values
	 * @param parameters
	 * @return
	 */
	public Point calculatePoint(double... parameter);
}
