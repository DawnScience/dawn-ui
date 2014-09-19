/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ParametricROIHandler;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

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
