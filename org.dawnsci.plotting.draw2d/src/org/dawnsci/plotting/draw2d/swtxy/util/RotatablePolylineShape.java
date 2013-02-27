/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
