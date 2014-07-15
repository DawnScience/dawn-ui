package org.dawnsci.plotting.draw2d.swtxy.selection;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.diamond.scisoft.analysis.roi.FreeDrawROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.diamond.scisoft.analysis.roi.handler.ROIHandler;

/**
 * Used for masking. This region can be transformed into the masking
 * dataset using MaskCreator (or code similar to).
 *
 */
public class FreeDrawSelection extends ROISelectionRegion<FreeDrawROI> {
	protected PointList points;

	public FreeDrawSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.orange);
		setLineWidth(10);
		setAlpha(160);
	}

	@Override
	protected ROIShape<FreeDrawROI> createShape(Figure parent) {
		return new Polyline(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.FREE_DRAW;
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		if (points == null) { // 
			points = new PointList();
			points.addPoint(clicks.getFirstPoint());
		}
		points.addPoint(clicks.getLastPoint());

		g.setLineWidth(getLineWidth());
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.drawPolyline(points);
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-free.png";
	}

	@Override
	public boolean isMobile() {
		return false; // You cannot move this figure yet...
	}

	@Override
	public int getMaximumMousePresses() {
		return 1;
	}

	@Override
	public void initialize(PointList clicks) {
		points = removeContiguousDuplicates(points);
		super.initialize(points);
	}

	private PointList removeContiguousDuplicates(PointList pnts) {
		PointList ret = new PointList();
		if (pnts==null || pnts.size()<1) return pnts;
		ret.addPoint(pnts.getPoint(0));
		for (int i = 1; i < pnts.size(); i++) {
			final Point point = pnts.getPoint(i);
			if (!point.equals(pnts.getPoint(i-1))) {
				ret.addPoint(point);
			}
		}
		return ret;
	}

	private void drawPointText(Graphics g, Point pnt) {
		double[] loc = coords.getValueFromPosition(pnt.x, pnt.y);
        final String text = getLabelPositionText(loc);
        g.drawString(text, pnt);
	}
	
	private NumberFormat format = new DecimalFormat("######0.00");
	
	protected String getLabelPositionText(double[] p) {
		if (Double.isNaN(p[0])||Double.isNaN(p[1])) return "";
		final StringBuilder buf = new StringBuilder();
		buf.append("(");
		buf.append(format.format(p[0]));
		buf.append(", ");
		buf.append(format.format(p[1]));
		buf.append(")");
		return buf.toString();
	}

	class Polyline extends ROIShape<FreeDrawROI> {
		public Polyline(Figure parent, AbstractSelectionRegion<FreeDrawROI> region) {
			super(parent, region);
			setLineWidth(region.getLineWidth());
			setAlpha(region.getAlpha());
		}

		@Override
		public void setup(PointList clicks) {
			croi = new FreeDrawROI();

			final Point p = new Point();
			for (int i = 0, imax = points.size(); i < imax; i++) {
				points.getPoint(p, i);
				croi.insertPoint(new PointROI(cs.getValueFromPosition(p.x(), p.y())));
			}

			region.createROI(true);
			region.setRegionObjects(this);
			int w = getLineWidth();
			setBounds(points.getBounds().expand(w, w));
		}

		@Override
		public boolean containsPoint(int x, int y) {
			if (croi == null)
				return super.containsPoint(x, y);
			double[] pt = cs.getValueFromPosition(x, y);
			return croi.isNearOutline(pt[0], pt[1], getLineWidth() / 2.);
		}

		@Override
		public void updateFromROI(FreeDrawROI proi) {
			int imax = croi == null ? 0 : croi.getNumberOfPoints();
			if (points == null) {
				points = new PointList(proi.getNumberOfPoints());
			}
			if (croi == null) {
				croi = proi;
			}

			if (imax != proi.getNumberOfPoints()) {
				if (proi != croi)
					croi.removeAllPoints();
				points.removeAllPoints();
				for (IROI r: proi) {
					if (proi != croi)
						croi.insertPoint(r);
					double[] pnt  = cs.getPositionFromValue(r.getPointRef());
					points.addPoint((int) pnt[0], (int) pnt[1]);
				}
			} else {
				for (int i = 0; i < imax; i++) {
					IROI p = proi.getPoint(i);
					if (proi != croi)
						croi.setPoint(i, p);
					double[] pnt = cs.getPositionFromValue(p.getPointRef());
					Point pt = new PrecisionPoint(pnt[0], pnt[1]);
					points.setPoint(pt, i);
				}
			}
			int w = getLineWidth();
			setBounds(points.getBounds().expand(w, w));
		}

		@Override
		protected void fillShape(Graphics graphics) {
			// do nothing
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			graphics.setForegroundColor(getRegionColor());
			graphics.setAlpha(getAlpha());
			graphics.setLineWidth(getLineWidth());
			graphics.drawPolyline(points);

			graphics.setAlpha(255);
			graphics.setForegroundColor(ColorConstants.black);
			if (isShowPosition()) {
				drawPointText(graphics, points.getFirstPoint());
				drawPointText(graphics, points.getLastPoint());
			}
			
			if (isShowLabel()) {
				graphics.drawText(getName(), points.getMidpoint());
			}
		}

		@Override
		protected ROIHandler<FreeDrawROI> createROIHandler(FreeDrawROI roi) {
			return null;
		}

		@Override
		public void setCentre(Point nc) {
		}
	}
}
