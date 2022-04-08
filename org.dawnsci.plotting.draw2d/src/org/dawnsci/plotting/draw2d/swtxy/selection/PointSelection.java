/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationListener;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.PointROIHandler;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ROIHandler;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

public class PointSelection extends ROISelectionRegion<PointROI> {

	PointSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(RegionType.POINT.getDefaultColor());
		setLineWidth(7);
		setAlpha(120);
	}

	@Override
	protected ROIShape<PointROI> createShape(Figure parent) {
		return new RPoint(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.POINT;
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		if (clicks.size() < 1)
			return;
		final Point pnt = clicks.getLastPoint();
		final int offset = getLineWidth() / 2; // int maths ok here
		g.setForegroundColor(getRegionColor());
		g.fillRectangle(pnt.x - offset, pnt.y - offset, getLineWidth(), getLineWidth());
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-point.png";
	}

	@Override
	public int getMaximumMousePresses() {
		return 1;
	}

	@Override
	public void setMobile(boolean mobile) {
		if (mobile == isMobile())
			return;

		super.setMobile(mobile);
		if (!mobile) {
			((RPoint) shape).setHandleVisible(); // always show handle
		}
	}

	class RPoint extends ROIShape<PointROI> {

		public RPoint(final Figure parent, AbstractSelectionRegion<PointROI> region) {
			super(parent, region);
			setAreaTranslatable(true);
		}

		@Override
		protected ROIHandler<PointROI> createROIHandler(PointROI roi) {
			return new PointROIHandler(roi);
		}

		@Override
		public void setCentre(Point nc) {
		}

		@Override
		public void setup(PointList points) {
			croi = new PointROI();
			final Point p = points.getFirstPoint();
			croi.setPoint(cs.getValueFromPosition(p.x(), p.y()));
			region.createROI(true);
			configureHandles();
		}

		@Override
		public void configureHandles() {
			if(shape.isGridSnap())
				snapToGrid();
			boolean mobile = region.isMobile();
			boolean visible = isVisible() && mobile;
			double[] pt = cs.getPositionFromValue(croi.getPointRef());
			Rectangle b = addHandle(pt[0], pt[1], mobile, visible, handleListener).getBounds();

			region.setRegionObjects(this, handles);
			if (b != null)
				setBounds(b);
		}

		@Override
		protected RectangularHandle addHandle(double x, double y, boolean mobile, boolean visible,
				TranslationListener listener) {
			RectangularHandle h = super.addHandle(x, y, mobile, visible, listener);
			h.setLocationAbsolute(true);
			return h;
		}

		public void setHandleVisible() {
			if (handles.size() > 0) {
				IFigure h = handles.get(0);
				if (!h.isVisible()) { // ensure handle is always visible
					h.setVisible(true);
				}
			}
		}

		@Override
		protected void fillShape(Graphics graphics) {
		}

		@Override
		protected void outlineShape(Graphics graphics) {
		}

		@Override
		public void snapToGrid() {
			PointROI cSnappedROI = croi;
			if (cSnappedROI != null) {
				cSnappedROI.setPoint(new double[]{Math.round(cSnappedROI.getPoint()[0]), Math.round(cSnappedROI.getPoint()[1])});
				croi = cSnappedROI;
			}
		}
	}
}
