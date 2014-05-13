package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.handler.LinearROIHandler;
import uk.ac.diamond.scisoft.analysis.roi.handler.ROIHandler;

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
		protected void configureHandles() {
			super.configureHandles();

			// hide centre handle
			int hc = roiHandler.getCentreHandle();
			SelectionHandle h = (SelectionHandle) handles.get(hc);
			h.setVisible(false);
			h.setVisibilityLock(true);
			fTranslators.get(hc).setActive(false);
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

		private static final double CROSSHAIR_LENGTH = 0.25;

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
	}
}
