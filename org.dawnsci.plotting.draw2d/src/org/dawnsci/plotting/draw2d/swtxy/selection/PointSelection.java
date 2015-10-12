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
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ROIHandler;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
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
	protected PointROI createROI(boolean recordResult) {
		// snap to grid
		if (shape.isGridSnap()) {
			PointROI snappedROI = shape.croi;
			snappedROI.setPoint(new double[]{(int) snappedROI.getPoint()[0], (int) snappedROI.getPoint()[1]});
			shape.croi = snappedROI;
			if (recordResult) {
				roi = shape.croi;
			}
			shape.configureHandles();
			return shape.croi;
		}
		return super.createROI(recordResult);
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
			return null;
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
		protected TranslationListener createHandleNotifier() {
			return region.createRegionNotifier();
		}

		@Override
		public void configureHandles() {
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

		@Override
		protected Rectangle updateFromHandles() {
			Rectangle b = null;
			for (IFigure f : handles) { // this is called first so update points
				if (f instanceof SelectionHandle) {
					SelectionHandle h = (SelectionHandle) f;
					Point pt = h.getSelectionPoint();
					double[] p = cs.getValueFromPosition(pt.x(), pt.y());
					croi.setPoint(p);
					if (b == null) {
						b = new Rectangle(h.getBounds());
					} else {
						b.union(h.getBounds());
					}
				}
			}
			return b;
		}

		/**
		 * Update according to ROI
		 * @param proi
		 */
		public void updateFromROI(PointROI proi) {
			int imax = handles.size();
			if (croi == null) {
				croi = proi;
			}

			double[] p = proi.getPoint();
			if (croi != proi)
				croi.setPoint(p[0], p[1]);
			double[] pnt  = cs.getPositionFromValue(p);
			Rectangle b = null;
			if (imax != 1) {
				for (int i = imax-1; i >= 0; i--) {
					removeHandle((SelectionHandle) handles.remove(i));
				}
				boolean mobile = region.isMobile();
				boolean visible = isVisible() && mobile;
				b = addHandle(pnt[0], pnt[1], mobile, visible, handleListener).getBounds();
				region.setRegionObjects(this, handles);
			} else {
				Point pt = new PrecisionPoint(pnt[0], pnt[1]);
				SelectionHandle h = (SelectionHandle) handles.get(0);
				h.setSelectionPoint(pt);
				b = h.getBounds();
			}

			if (b != null)
				setBounds(b);
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
			PointROI tSnappedROI = troi;
			PointROI cSnappedROI = croi;
			if (tSnappedROI != null) {
				tSnappedROI.setPoint(new double[]{(int) tSnappedROI.getPoint()[0], (int) tSnappedROI.getPoint()[1]});
				troi = tSnappedROI;
			}
			if (cSnappedROI != null) {
				cSnappedROI.setPoint(new double[]{(int) cSnappedROI.getPoint()[0], (int) cSnappedROI.getPoint()[1]});
				croi = cSnappedROI;
			}
		}
	}
}
