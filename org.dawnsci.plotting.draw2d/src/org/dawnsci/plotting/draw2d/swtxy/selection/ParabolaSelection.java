/*-
 * Copyright 2014 Diamond Light Source Ltd.
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

import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.diamond.scisoft.analysis.roi.ParabolicROI;
import uk.ac.diamond.scisoft.analysis.roi.handler.ParametricROIHandler;

public class ParabolaSelection extends LockableSelectionRegion<ParabolicROI> {
	public ParabolaSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.orange);
	}

	@Override
	protected ParametricROIShape<ParabolicROI> createShape(Figure parent) {
		return parent == null ? new PRShape() : new PRShape(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.PARABOLA;
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-ellipse.png";
	}

	class PRShape extends ParametricROIShape<ParabolicROI> {
		public PRShape() {
			super();
			setCoordinateSystem(coords);
		}

		public PRShape(Figure parent, AbstractSelectionRegion<ParabolicROI> region) {
			super(parent, region);
			setFill(false);
			showMajorAxis(true);
		}

		@Override
		protected ParametricROIHandler<ParabolicROI> createROIHandler(ParabolicROI roi) {
			return new ParametricROIHandler<ParabolicROI>(roi, false);
		}

		@Override
		public void setup(PointList corners, boolean withHandles) {
			final Point pa = corners.getFirstPoint();
			final Point pc = corners.getLastPoint();

			double[] a = cs.getValueFromPosition(pa.x(), pa.y());
			double[] c = cs.getValueFromPosition(pc.x(), pc.y());
			double cx = Math.min(a[0], c[0]);
			double cy = Math.min(a[1], c[1]);
			double p = Math.min(Math.abs(a[0] - c[0]), 0.25*Math.abs(a[1] - c[1]));

			croi = new ParabolicROI(p, cx + p, cy + 2 * p);

			if (withHandles) {
				roiHandler.setROI(createROI(true));
				configureHandles();
			}
		}

		@Override
		protected void outlineShape(Graphics graphics, Rectangle parentBounds) {
			outlineShape(graphics, parentBounds, false);

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
//			double rad = cs.getPositionFromValue(getROI().getSemiAxes())[0];
//			return "EllSel: cen=" + getCentre() + ", rad=" + rad;
			return "";
		}
	}
}
