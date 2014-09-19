/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.dawnsci.analysis.dataset.roi.HyperbolicROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ParametricROIHandler;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

public class HyperbolaSelection extends LockableSelectionRegion<HyperbolicROI> {

	public HyperbolaSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.lightBlue);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.HYPERBOLA;
	}

	@Override
	protected ParametricROIShape<HyperbolicROI> createShape(Figure parent) {
		return parent == null ? new PRShape() : new PRShape(parent, this);
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-ellipse.png";
	}

	class PRShape extends ParametricROIShape<HyperbolicROI> {
		public PRShape() {
			super();
			setCoordinateSystem(coords);
		}

		public PRShape(Figure parent, AbstractSelectionRegion<HyperbolicROI> region) {
			super(parent, region);
			setFill(false);
			showMajorAxis(true);
		}

		@Override
		protected ParametricROIHandler<HyperbolicROI> createROIHandler(HyperbolicROI roi) {
			return new ParametricROIHandler<HyperbolicROI>(roi, false);
		}

		@Override
		public void setup(PointList corners, boolean withHandles) {
			final Point pa = corners.getFirstPoint();
			final Point pc = corners.getLastPoint();

			double[] a = cs.getValueFromPosition(pa.x(), pa.y());
			double[] c = cs.getValueFromPosition(pc.x(), pc.y());
			double cx = Math.max(a[0], c[0]);
			double cy = 0.5 * (a[1] + c[1]);
			double l = 0.5 * Math.abs(a[1] - c[1]);
			double e = Math.max(l/Math.abs(a[0] - c[0]) - 1, 1);

			croi = new HyperbolicROI(l, e, cx, cy);

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
