/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy.util;

import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.geometry.Point;

/**
 * A parametric point function that generates a 2D point from given parameters
 */
public interface PointFunction {

	/**
	 * An empty implementation that does nothing and return nulls
	 */
	public class Stub implements PointFunction {
		public Stub() {
		}

		@Override
		public void setCoordinateSystem(ICoordinateSystem system) {
		}

		@Override
		public Point calculatePoint(double... parameter) {
			return null;
		}

		@Override
		public double[] calculateXIntersectionParameters(int x) {
			return null;
		}

		@Override
		public double[] calculateYIntersectionParameters(int y) {
			return null;
		}
	}

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
