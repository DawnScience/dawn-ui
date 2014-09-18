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

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ParametricROIHandler;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

public class CircleSelection extends LockableSelectionRegion<CircularROI> {

	public CircleSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.yellow);
	}

	@Override
	protected ParametricROIShape<CircularROI> createShape(Figure parent) {
		return parent == null ? new PRShape() : new PRShape(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.CIRCLE;
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-circle.png";
	}

	@Override
	public int getMaximumMousePresses() {
		return 2;
	}

	class PRShape extends ParametricROIShape<CircularROI> {

		public PRShape() {
			super();
			setCoordinateSystem(coords);
		}

		public PRShape(Figure parent, AbstractSelectionRegion<CircularROI> region) {
			super(parent, region);
			setFill(false);
		}

		@Override
		protected ParametricROIHandler<CircularROI> createROIHandler(CircularROI roi) {
			return new ParametricROIHandler<CircularROI>(roi, true);
		}

		@Override
		public void setup(PointList corners, boolean withHandles) {
			final Point pa = corners.getFirstPoint();
			final Point pc = corners.getLastPoint();

			double[] a = cs.getValueFromPosition(pa.x(), pa.y());
			double[] c = cs.getValueFromPosition(pc.x(), pc.y());
			double cx = Math.min(a[0], c[0]);
			double cy = Math.min(a[1], c[1]);
			double rad = 0.5 * Math.min(Math.abs(a[0] - c[0]), Math.abs(a[1] - c[1]));

			croi = new CircularROI(rad, cx+rad, cy+rad);

			if (withHandles) {
				roiHandler.setROI(createROI(true));
				configureHandles();
			}
		}

		@Override
		protected void outlineShape(Graphics graphics, Rectangle parentBounds) {
			outlineShape(graphics, parentBounds, true);

			if (label != null && isShowLabel()) {
				graphics.setAlpha(192);
				graphics.setForegroundColor(labelColour);
				graphics.setBackgroundColor(ColorConstants.white);
				graphics.setFont(labelFont);
				graphics.fillString(label, getPoint(Math.PI/4));
			}
		}

		@Override
		public String toString() {
			double rad = cs.getPositionFromValue(getROI().getRadius())[0];
			return "CirSel: cen=" + getCentre() + ", rad=" + rad;
		}
	}
}
