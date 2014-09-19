/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.util;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A Draw2D polyline shape that allows its orientation to be set. Its location is the centre of
 * rotation (not the top-left corner of its bounding box)
 */
public class RotatablePolylineShape extends RotatablePolygonShape {
	private int tolerance = 2;

	public RotatablePolylineShape() {
		super();
	}

	public RotatablePolylineShape(PointList list, double angle) {
		super(list, angle);
	}

	@Override
	protected void fillShape(Graphics graphics) {
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		Rectangle b = getParent().getBounds();
		Draw2DUtils.drawClippedPolyline(graphics, npl, b, false);
	}

	@Override
	protected boolean shapeContainsPoint(int x, int y) {
		return npl.polylineContainsPoint(x, y, tolerance);
	}

	/**
	 * Set tolerance of hit detection of shape
	 * @param tolerance (number of pixels between point and segment)
	 */
	public void setTolerance(int tolerance) {
		this.tolerance = tolerance;
	}
}
