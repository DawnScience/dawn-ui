/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ROIHandler;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.RectangularROIHandler;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

class BoxSelection extends ROISelectionRegion<RectangularROI> {

	BoxSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(RegionType.BOX.getDefaultColor());
	}

	@Override
	protected ROIShape<RectangularROI> createShape(Figure parent) {
		return new Box(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.BOX;
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		if (clicks.size() <= 1)
			return;

		g.setLineStyle(SWT.LINE_DOT);
		final Rectangle bounds = new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint());
		g.drawRectangle(bounds);
		g.setBackgroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.fillRectangle(bounds);
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-box.png";
	}

	@Override
	public int getMaximumMousePresses() {
		return 2;
	}

	protected void drawRectangle(Graphics g) {
		((Box) shape).internalOutline(g);
	}

	protected void fillRectangle(Graphics g) {
		((Box) shape).internalFill(g);
	}

	@Override
	public RectangularROI getROI() {
		return shape != null ? shape.getROI() : super.getROI();
	}

	@Override
	protected RectangularROI createROI(boolean recordResult) {
		return super.createROI(recordResult);
	}

	class Box extends ROIShape<RectangularROI> {

		public Box(Figure parent, AbstractSelectionRegion<RectangularROI> region) {
			super(parent, region);
		}

		@Override
		protected ROIHandler<RectangularROI> createROIHandler(RectangularROI roi) {
			return new RectangularROIHandler(roi);
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
			double ox = Math.min(a[0], c[0]);
			double oy = Math.min(a[1], c[1]);
			double lx = Math.abs(a[0] - c[0]);
			double ly = Math.abs(a[1] - c[1]);
			croi = new RectangularROI(ox, oy, lx, ly, 0);
			roiHandler.setROI((RectangularROI) createROI(true));
			configureHandles();
		}

		@Override
		public String toString() {
			if (croi == null)
				return "BoxSel: undefined";

			double[] pt = cs.getPositionFromValue(croi.getPointRef());
			Point start = new PrecisionPoint(pt[0], pt[1]);
			double[] pta = cs.getPositionFromValue(0, 0);
			double[] ptb = cs.getPositionFromValue(croi.getLengths());

			return "BoxSel: start=" + start + ", major=" + (ptb[0] - pta[0]) + ", minor=" + (ptb[1] - pta[1]) + ", ang=" + croi.getAngleDegrees();
		}

		protected PointList generatePointList() {
			PointList pl = new PointList(4);
			double[] pt;
			RectangularROI proi = getROI();
			pt = cs.getPositionFromValue(proi.getPointRef());
			pl.addPoint((int) pt[0], (int) pt[1]);
			pt = cs.getPositionFromValue(proi.getPoint(1, 0));
			pl.addPoint((int) pt[0], (int) pt[1]);
			pt = cs.getPositionFromValue(proi.getPoint(1, 1));
			pl.addPoint((int) pt[0], (int) pt[1]);
			pt = cs.getPositionFromValue(proi.getPoint(0, 1));
			pl.addPoint((int) pt[0], (int) pt[1]);

			return pl;
		}

		private void internalFill(Graphics graphics) {
			graphics.pushState();
			graphics.setAdvanced(true);
			graphics.setAntialias(SWT.ON);
			graphics.fillPolygon(generatePointList());
			graphics.popState();
		}

		private void internalOutline(Graphics graphics) {
			graphics.pushState();
			graphics.setAdvanced(true);
			graphics.setAntialias(SWT.ON);

			Rectangle bnds = parent.getBounds();
			Draw2DUtils.drawClippedPolyline(graphics, generatePointList(), bnds, true);
			graphics.popState();
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			drawRectangle(graphics);
			drawLabel(graphics, getROI().getMidPoint());
		}

		@Override
		protected void fillShape(Graphics graphics) {
			fillRectangle(graphics);
		}

		@Override
		public void snapToGrid() {
			RectangularROI cSnappedROI = croi;
			if (cSnappedROI != null) {
				cSnappedROI.setPoint(Math.round(cSnappedROI.getPoint()[0]), Math.round(cSnappedROI.getPoint()[1]));
				cSnappedROI.setEndPoint(new double[] {Math.round(cSnappedROI.getEndPoint()[0]), Math.round(cSnappedROI.getEndPoint()[1])});
				croi = cSnappedROI;
			}
		}
	}
}
