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

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
import uk.ac.diamond.scisoft.analysis.roi.handler.ParametricROIHandler;

public class EllipseSelection extends LockableSelectionRegion<EllipticalROI> {

	public EllipseSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.lightGreen);
	}

	@Override
	protected ParametricROIShape<EllipticalROI> createShape(Figure parent) {
		return parent == null ? new PRShape() : new PRShape(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.ELLIPSE;
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-ellipse.png";
	}

	class PRShape extends ParametricROIShape<EllipticalROI> {
		public PRShape() {
			super();
			setCoordinateSystem(coords);
		}

		public PRShape(Figure parent, AbstractSelectionRegion<EllipticalROI> region) {
			super(parent, region);
			setFill(false);
			showMajorAxis(true);
		}

		@Override
		protected ParametricROIHandler<EllipticalROI> createROIHandler(EllipticalROI roi) {
			return new ParametricROIHandler<EllipticalROI>(roi, true);
		}

		@Override
		public void setup(PointList corners, boolean withHandles) {
			final Point pa = corners.getFirstPoint();
			final Point pc = corners.getLastPoint();

			double[] a = cs.getValueFromPosition(pa.x(), pa.y());
			double[] c = cs.getValueFromPosition(pc.x(), pc.y());
			double cx = 0.5 * (a[0] + c[0]);
			double cy = 0.5 * (a[1] + c[1]);
			double maj = 0.5 * Math.abs(a[0] - c[0]);
			double min = 0.5 * Math.abs(a[1] - c[1]);
			double ang = maj >= min ? 0 : Math.PI/2;

			croi = ang == 0 ? new EllipticalROI(maj, min, ang, cx, cy) : new EllipticalROI(min, maj, ang, cx, cy);

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
				graphics.fillString(label, getPoint(Math.PI * 0.75));
			}
		}

		@Override
		public String toString() {
			double rad = cs.getPositionFromValue(getROI().getSemiAxes())[0];
			return "EllSel: cen=" + getCentre() + ", rad=" + rad;
		}
	}
}
