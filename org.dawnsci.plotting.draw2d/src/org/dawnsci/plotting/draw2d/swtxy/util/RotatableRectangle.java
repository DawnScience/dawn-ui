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
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

/**
 * A Draw2D rectangle shape that allows its orientation to be set. Its location is the centre of
 * rotation (not the top-left corner of its bounding box)
 */
public class RotatableRectangle extends RotatablePolygonShape {

	private PointList ool; // outline points
	private PointList nol; // transformed outline points

	/**
	 * Constructor for rectangle
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param angle in degrees (positive is anti-clockwise)
	 */
	public RotatableRectangle(int x, int y, int width, int height, double angle) {
		super(4);

		opl.addPoint(0, 0);
		opl.addPoint(width, 0);
		opl.addPoint(width, height);
		opl.addPoint(0, height);
		npl.removeAllPoints();
		npl.addAll(opl);
		ool = new PointList(4);
		ool.addAll(opl);
		nol = new PointList(4);
		nol.addAll(opl);
		recalcOutline();
		affine.setTranslation(x, y);
		setAngle(angle);
	}

	@Override
	public void setLineWidth(int w) {
		super.setLineWidth(w);
		recalcOutline();
	}

	@Override
	public void setLineWidthFloat(float value) {
		super.setLineWidthFloat(value);
		recalcOutline();
	}

	private void recalcOutline() {
		final double lineInset = Math.max(1.0, getLineWidthFloat()) / 2.0;
		final int inset1 = (int) Math.floor(lineInset);
		final int inset2 = (int) Math.ceil(lineInset);
		final Point c = opl.getPoint(2);
		final int x = c.x() - inset2;
		final int y = c.y() - inset2;
		ool.removeAllPoints();
		ool.addPoint(inset1, inset1);
		ool.addPoint(x, inset1);
		ool.addPoint(x, y);
		ool.addPoint(inset1, y);
	}

	@Override
	protected void recalcPoints(PointList oldpl, PointList newpl, boolean setBounds) {
		super.recalcPoints(ool, nol, false);
		super.recalcPoints(oldpl, newpl, true);
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.drawPolygon(nol);
	}
}
