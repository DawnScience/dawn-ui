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
import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.handler.ParametricROIHandler;

public class CircleSelection extends AbstractSelectionRegion<CircularROI> {

	PRShape shape;

	public CircleSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.yellow);
		setAlpha(80);
		setLineWidth(2);
	}

	@Override
	public void setMobile(boolean mobile) {
		super.setMobile(mobile);
		if (shape != null)
			shape.setMobile(mobile);
	}

	@Override
	public void createContents(Figure parent) {
		shape = new PRShape(parent, this);
		shape.setCursor(Draw2DUtils.getRoiMoveCursor());

		parent.add(shape);
		sync(getBean());
		shape.setLineWidth(getLineWidth());
	}

	@Override
	public boolean containsPoint(int x, int y) {
		return shape.containsPoint(x, y);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.CIRCLE;
	}

	@Override
	protected void updateBounds() {
		if (shape != null) {
			Rectangle b = shape.updateFromHandles();
			if (b != null)
				shape.setBounds(b);
		}
	}

	private PRShape tempShape = null;

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		if (clicks.size() <= 1)
			return;

		g.setLineStyle(SWT.LINE_DOT);
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		if (tempShape == null) {
			tempShape = new PRShape();
		}
		tempShape.setup(clicks, false);
		
		tempShape.outlineShape(g, parentBounds);
	}

	@Override
	public void initialize(PointList clicks) {
		if (shape != null) {
			shape.setup(clicks);
			fireROIChanged(getROI());
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-circle.png";
	}

	@Override
	protected CircularROI createROI(boolean recordResult) {
		if (recordResult) {
			roi = shape.croi;
		}
		return shape.croi;
	}

	@Override
	protected void updateRegion() {
		if (shape != null && roi instanceof CircularROI) {
			shape.updateFromROI((CircularROI) roi);
			sync(getBean());
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 2;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (shape != null) {
			shape.dispose();
		}
		if (tempShape != null) {
			tempShape.dispose();
		}
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
