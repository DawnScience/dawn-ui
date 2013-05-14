package org.dawnsci.plotting.draw2d.swtxy.selection;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Geometry;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.diamond.scisoft.analysis.roi.FreeDrawROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;

/**
 * Used for masking. This region can be transformed into the masking
 * dataset using MaskCreator (or code similar to).
 * 
 * The region bounds from this selection is a polyline region
 * bounds consisting of the 
 * 
 * @author fcp94556
 *
 */
class FreeDrawSelection extends AbstractSelectionRegion {

	private PointList points;

	public FreeDrawSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.orange);
		setLineWidth(10);
		setAlpha(160);
	}

	@Override
	public void createContents(Figure parent) {
		parent.add(this);
		updateConnectionBounds();
	}
	@Override
	public boolean containsPoint(double x, double y) {
		
		final int[] pix = coords.getValuePosition(x,y);
		if (!getBounds().contains(pix[0],pix[1])) return false;
		return Geometry.polylineContainsPoint(points, pix[0], pix[1], (int)Math.round(getLineWidth()/2d));
	}
	
	@Override
	public boolean isMobile() {
		return false; // You cannot move this figure yet...
	}

	@Override
	public void setLineWidth(int width) {
		super.setLineWidth(width);
		updateConnectionBounds();
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.FREE_DRAW;
	}

	@Override
	protected void updateConnectionBounds() {
		
		if (points==null) return;
		final Rectangle pntBounds = points.getBounds().getCopy();
		pntBounds.x     -=getLineWidth(); 
		pntBounds.y     -=getLineWidth(); 
		pntBounds.width +=2*getLineWidth(); 
		pntBounds.height+=2*getLineWidth(); 
		setBounds(pntBounds);
	}

	@Override
	public void paintBeforeAdded(Graphics g, 
			                     PointList clicks,
			                     Rectangle parentBounds) {
		
		if (points==null) {
			points = new PointList();
			points.addPoint(clicks.getFirstPoint());
		}
		points.addPoint(clicks.getLastPoint());
		
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.setLineWidth(getLineWidth());
		g.drawPolyline(points);
	}
	
	@Override
	public void paintFigure(Graphics g) {
		
		super.paintFigure(g);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.setLineWidth(getLineWidth());
		g.drawPolyline(points);
		
		g.setAlpha(255);
		g.setForegroundColor(ColorConstants.black);
		if (isShowPosition()) {
			drawPointText(g, points.getFirstPoint());
			drawPointText(g, points.getLastPoint());
		}
		
		if (isShowLabel()) {
			g.drawText(getName(), points.getMidpoint());
		}
	}

	private void drawPointText(Graphics g, Point pnt) {
		
		double[] loc = coords.getPositionValue(pnt.x, pnt.y);
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

	@Override
	public void setLocalBounds(PointList clicks, 
			                   Rectangle parentBounds) {
		
		points = removeContiguousDuplicates(points);
		updateConnectionBounds();
		createROI(true);
		fireROIChanged(getROI());
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

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-free.png";
	}

	@Override
	protected IROI createROI(boolean recordResult) {
		if (points == null) return getROI();
		
		final FreeDrawROI proi = new FreeDrawROI();
		
		for (int i = 0, imax = points.size(); i < imax; i++) {
			final Point pnt = points.getPoint(i);
			proi.insertPoint(i, coords.getPositionValue(pnt.x(),pnt.y()));
		}
		if (roi!=null) proi.setPlot(roi.isPlot());
		// set the Region isActive flag
		this.setActive(this.isActive());
		if (recordResult)
			roi = proi;
		
		return proi;
	}

	@Override
	protected void updateROI(IROI roi) {
		if (roi instanceof PolylineROI) {
			final PolylineROI proi = (PolylineROI) roi;
			if (points==null) points = new PointList();
	        points.removeAllPoints();
	        
	        for (IROI p : proi) {
	           	final int[] pix = coords.getValuePosition(p.getPoint());
	           	points.addPoint(new Point(pix[0],pix[1]));
			}
	        updateConnectionBounds();
		}

	}

	@Override
	public int getMaximumMousePresses() {
		return 1;
	}
}
