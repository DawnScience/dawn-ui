/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.LinearROIHandler;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ROIHandler;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**            (if isCrossHair())
*                      |
*                      |
*                      |
*    startBox-----------------------endBox
*                      |
*                      |
*                      |
*              (if isCrossHair())
*/
class LineSelection extends ROISelectionRegion<LinearROI> {

	LineSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.cyan);
	}

	@Override
	protected ROIShape<LinearROI> createShape(Figure parent) {
		return new RShape(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.LINE;
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		if (clicks.size() <= 1)
			return;

		g.setLineStyle(SWT.LINE_DOT);
		g.setLineWidth(2);
		g.setAlpha(getAlpha());
		g.drawLine(clicks.getFirstPoint(), clicks.getLastPoint());
	}

	@Override
	protected LinearROI createROI(boolean recordResult) {
		// snap to grid
		if (shape.isGridSnap()) {
			LinearROI snappedROI = shape.croi;
			snappedROI.setPoint((int) snappedROI.getPointX(), (int) snappedROI.getPointY());
			snappedROI.setEndPoint((int) snappedROI.getEndPoint()[0], (int) snappedROI.getEndPoint()[1]);
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
	protected String getCursorPath() {
		return "icons/Cursor-line.png";
	}

	@Override
	public int getMaximumMousePresses() {
		return 2;
	}

	class RShape extends ROIShape<LinearROI> {

		public RShape(Figure parent, AbstractSelectionRegion<LinearROI> region) {
			super(parent, region);
		}

		@Override
		protected ROIHandler<LinearROI> createROIHandler(LinearROI roi) {
			return new LinearROIHandler(roi);
		}

		@Override
		public void setCentre(Point nc) {
			double[] pt = cs.getValueFromPosition(nc.x(), nc.y());
			double[] pc = croi.getMidPoint();
			pt[0] -= pc[0];
			pt[1] -= pc[1];
			croi.addPoint(pt);
			dirty = true;
			calcBox(croi, true);
		}

		@Override
		public void setup(PointList corners) {
			final Point pa = corners.getFirstPoint();
			final Point pc = corners.getLastPoint();

			double[] a = cs.getValueFromPosition(pa.x(), pa.y());
			double[] c = cs.getValueFromPosition(pc.x(), pc.y());
			croi = new LinearROI(a, c);

			roiHandler.setROI((LinearROI) createROI(true));
			configureHandles();
		}

		@Override
		public String toString() {
			if (croi == null)
				return "LineSel: undefined";

			double[] pt = cs.getPositionFromValue(croi.getPointRef());
			Point start = new PrecisionPoint(pt[0], pt[1]);
			pt = cs.getPositionFromValue(croi.getEndPoint());
			Point end = new PrecisionPoint(pt[0], pt[1]);

			return "LineSel: start=" + start + ", end=" + end + ", ang=" + croi.getAngleDegrees();
		}

		private static final double CROSSHAIR_LENGTH = 0.5;

		@Override
		protected void outlineShape(Graphics graphics) {
			graphics.pushState();
			graphics.setAdvanced(true);
			graphics.setAntialias(SWT.ON);

			double[] pt;
			LinearROI lroi = getROI();
			pt = cs.getPositionFromValue(lroi.getPointRef());

			PrecisionPoint pa = new PrecisionPoint(pt[0], pt[1]);

			pt = cs.getPositionFromValue(lroi.getEndPoint());
			PrecisionPoint pb = new PrecisionPoint(pt[0], pt[1]);
			graphics.drawLine(pa, pb);
			if (lroi.isCrossHair()) {
				pt = cs.getPositionFromValue(lroi.getPerpendicularBisectorPoint(0.5 - CROSSHAIR_LENGTH));
				pa.setPreciseLocation(pt[0], pt[1]);
				pt = cs.getPositionFromValue(lroi.getPerpendicularBisectorPoint(0.5 + CROSSHAIR_LENGTH));
				pb.setPreciseLocation(pt[0], pt[1]);
				graphics.drawLine(pa, pb);
			}
			graphics.popState();
			drawLabel(graphics, getROI().getMidPoint());
		}

		@Override
		protected void fillShape(Graphics graphics) {
		}

		private final static int TOLERANCE = 2;

		@Override
		public boolean containsPoint(int x, int y) {
			if (croi == null)
				return super.containsPoint(x, y);
			double[] pt = cs.getValueFromPosition(x, y);
			return croi.isNearOutline(pt[0], pt[1], TOLERANCE);
		}

		@Override
		protected void calcBox(LinearROI lroi, boolean redraw) {
			super.calcBox(lroi, redraw);

			if (bnds == null || lroi == null || !lroi.isCrossHair())
				return;

			double[] pt = cs.getPositionFromValue(lroi.getPerpendicularBisectorPoint(0.5 - CROSSHAIR_LENGTH));
			bnds.union(pt[0], pt[1]);
			pt = cs.getPositionFromValue(lroi.getPerpendicularBisectorPoint(0.5 + CROSSHAIR_LENGTH));
			bnds.union(pt[0], pt[1]);
			if (redraw)
				setBounds(bnds);
		}

		@Override
		public void snapToGrid() {
			LinearROI tSnappedROI = troi;
			LinearROI cSnappedROI = croi;
			if (tSnappedROI != null) {
				tSnappedROI.setPoint((int) tSnappedROI.getPointX(), (int) tSnappedROI.getPointY());
				tSnappedROI.setEndPoint(new double[]{(int) tSnappedROI.getEndPoint()[0], (int) tSnappedROI.getEndPoint()[1]});
				troi = tSnappedROI;
			}
			if (cSnappedROI != null) {
				cSnappedROI.setPoint((int) cSnappedROI.getPointX(), (int) cSnappedROI.getPointY());
				cSnappedROI.setEndPoint(new double[]{(int) cSnappedROI.getEndPoint()[0], (int) cSnappedROI.getEndPoint()[1]});
				croi = cSnappedROI;
			}
		}
	}
}
