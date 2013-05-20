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

import org.eclipse.draw2d.AbstractPointListShape;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A Draw2D polygon shape that allows its orientation to be set. Its location is the centre of
 * rotation (not the top-left corner of its bounding box)
 */
public class RotatablePolygonShape extends AbstractPointListShape {
	protected final PointList opl; // original (unrotated) points
	protected PointList npl; // reference to rotated points
	protected final AffineTransform affine;

	protected RotatablePolygonShape(int size) {
		opl = new PointList(size);
		npl = getPoints();
		npl.setSize(size);
		affine = new AffineTransform();
	}

	/**
	 * Null constructor
	 */
	public RotatablePolygonShape() {
		this(0);
		npl.removeAllPoints();
		setAngle(0);
	}

	/**
	 * Generic constructor
	 * @param list
	 * @param angle in degrees (positive is anti-clockwise)
	 */
	public RotatablePolygonShape(PointList list, double angle) {
		this(list.size());
		npl.removeAllPoints();
		npl.addAll(list);
		opl.addAll(list);
		setAngle(angle);
	}

	@Override
	public void addPoint(Point pt) {
		opl.addPoint(pt);
		super.addPoint(pt);
		recalcPoints(opl, npl, true);
	}

	@Override
	public void removeAllPoints() {
		opl.removeAllPoints();
		super.removeAllPoints();
	}

	@Override
	public void insertPoint(Point pt, int index) {
		opl.insertPoint(pt, index);
		super.insertPoint(affine.getTransformed(pt), index);
	}

	@Override
	public void removePoint(int index) {
		opl.removePoint(index);
		super.removePoint(index);
	}

	@Override
	public void setPoint(Point pt, int index) {
		opl.setPoint(pt, index);
		super.setPoint(affine.getTransformed(pt), index);
	}

	@Override
	public void setPoints(PointList points) {
		opl.removeAllPoints();
		opl.addAll(points);
		super.setPoints(points);
		npl = super.getPoints();
		recalcPoints(opl, npl, true);
	}

	/**
	 * Set angle of rotated polyline to given degrees clockwise
	 * @param degrees
	 */
	public void setAngle(double degrees) {
		affine.setRotationDegrees(degrees);
		recalcPoints(opl, npl, true);
	}

	/**
	 * @return angle of rotation in degrees (positive clockwise)
	 */
	public double getAngleDegrees() {
		return affine.getRotationDegrees();
	}

	@Override
	public void setLocation(Point p) {
		affine.setTranslation(p.preciseX(), p.preciseY());
		recalcPoints(opl, npl, true);
	}

	protected void recalcPoints(PointList oldpl, PointList newpl, boolean setBounds) {
		final int n = newpl.size();
		for (int i = 0; i < n; i++) {
			newpl.setPoint(affine.getTransformed(oldpl.getPoint(i)), i);
		}
		if (setBounds) {
			Rectangle b = newpl.getBounds();
			setBounds(b);
		}
	}

	@Override
	protected void fillShape(Graphics graphics) {
		graphics.fillPolygon(npl);
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		Rectangle b = getParent().getBounds();
		Draw2DUtils.drawClippedPolyline(graphics, npl, b, true);
	}

	@Override
	protected boolean shapeContainsPoint(int x, int y) {
		return npl.polygonContainsPoint(x, y);
	}
}
