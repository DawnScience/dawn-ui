package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.api.region.IRegion;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PerimeterBoxROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 *     A BoxSelection with coloured edges
 * 
 *     p1------------p2
 *     |  o o o o o   |
 *     |  o o o o o   |
 *     p3------------p4
 *     
 *     This class is public so that it can be cast and the various
 *     colour settings accessed.
 *     
 *     Normally concrete class of IRegion should not be used
 *     
 */
public class PerimeterBoxSelection extends BoxSelection {
		
	private Color leftColor = ColorConstants.red;
	private Color rightColor = ColorConstants.darkGreen;
	private Color topColor = ColorConstants.blue;
	private Color bottomColor = ColorConstants.orange;
	
	/**
	 * Non public, it can be used from outside but not created there.
	 * @param name
	 * @param coords
	 */
	PerimeterBoxSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(IRegion.RegionType.PERIMETERBOX.getDefaultColor());	
		setAlpha(0);
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-colorbox.png";
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.PERIMETERBOX;
	}

	@Override
	protected void drawRectangle(Graphics g) {
		super.drawRectangle(g);

		PointList points = rect.getOutline();
		if (points.size() == 4)
			drawColouredEdges(g, points);
	}

	/**
	 * Draws the coloured edges
	 * 
	 *   start-------------
	 *     |               |
	 *     |               |
	 *     |               |
	 *      --------------end
	 *      
	 * @param g
	 * @param rect
	 */
	protected void drawColouredEdges(Graphics g, PointList points) {
		g.pushState();
		g.setAlpha(255);
		g.setLineWidth(2);
		g.setLineStyle(SWT.LINE_SOLID);

		Point p1 = points.getPoint(0), p2 = points.getPoint(1), 
			  p3 = points.getPoint(2), p4 = points.getPoint(3);
		// draw top edge
		g.setForegroundColor(topColor);
		g.setBackgroundColor(topColor);
		g.drawLine(p1.x(), p1.y(), p2.x(), p2.y());

		// draw bottom edge
		g.setForegroundColor(bottomColor);
		g.setBackgroundColor(bottomColor);
		g.drawLine(p4.x(), p4.y(), p3.x(), p3.y());

		// draw left edge
		g.setForegroundColor(leftColor);
		g.setBackgroundColor(leftColor);
		g.drawLine(p1.x(), p1.y(), p4.x(), p4.y());

		// draw right edge
		g.setForegroundColor(rightColor);
		g.setBackgroundColor(rightColor);
		g.drawLine(p2.x(), p2.y(), p3.x(), p3.y());

		g.popState();
	}

	/**
	 * Draws the coloured edges
	 * 
	 *   start-------------
	 *     |               |
	 *     |               |
	 *     |               |
	 *      --------------end
	 *      
	 * @param gc
	 * @param rect
	 */
	protected void drawColoredEdges(Graphics gc, Rectangle rect) {

		gc.pushState();
		gc.setAlpha(255);
		gc.setLineWidth(2);
		gc.setLineStyle(SWT.LINE_SOLID);
		int[] start = {rect.x, rect.y};
		int[] end = {rect.x + rect.width, rect.y + rect.height};

		if (rect.width != 0 && rect.height != 0) {
			
			// draw top edge
			gc.setForegroundColor(topColor);
			gc.setBackgroundColor(topColor);
			gc.drawLine(start[0], start[1], end[0], start[1]);
			
			// draw bottom edge
			gc.setForegroundColor(bottomColor);
			gc.setBackgroundColor(bottomColor);
			gc.drawLine(start[0], end[1], end[0], end[1]);
			
			// draw left edge
			gc.setForegroundColor(leftColor);
			gc.setBackgroundColor(leftColor);
			gc.drawLine(start[0], start[1], start[0], end[1]);
			
			// draw right edge
			gc.setForegroundColor(rightColor);
			gc.setBackgroundColor(rightColor);
			gc.drawLine(end[0], start[1], end[0], end[1]);
		}
		gc.popState();
	}

	@Override
	public IROI createROI(boolean recordResult) {
		IROI croi = super.createROI(recordResult);
		if (croi == null)
			return super.getROI();

		RectangularROI rroi = (RectangularROI) croi;
		final PerimeterBoxROI proi = new PerimeterBoxROI(rroi.getPointX(), rroi.getPointY(), rroi.getLength(0), rroi.getLength(1), rroi.getAngle());

		proi.setName(getName());
		if (recordResult) roi = proi;
		return proi;
	}

	public Color getLeftColor() {
		return leftColor;
	}

	public void setLeftColor(Color left) {
		this.leftColor = left;
	}
	
	public Color getRightColor() {
		return rightColor;
	}

	public void setRightColor(Color rightColor) {
		this.rightColor = rightColor;
	}

	public Color getTopColor() {
		return topColor;
	}

	public void setTopColor(Color top) {
		this.topColor = top;
	}
	
	public Color getBottomColor() {
		return bottomColor;
	}

	public void setbottomColor(Color bottomColor) {
		this.bottomColor = bottomColor;
	}
}
