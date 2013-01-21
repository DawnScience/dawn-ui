package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawb.common.ui.plot.axis.ICoordinateSystem;
import org.dawb.common.ui.plot.region.IRegion;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
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
 *     color settings accessed.
 *     
 *     Normally concrete class of IRegion should not be used
 *     
 */
public class ColoredBoxSelection extends BoxSelection {
		
	private Color leftColor = ColorConstants.blue;
	private Color rightColor = ColorConstants.red;
	private Color topColor = ColorConstants.darkGreen;
	private Color bottomColor = ColorConstants.orange;
	
	/**
	 * Non public, it can be used from outside but not created there.
	 * @param name
	 * @param coords
	 */
	ColoredBoxSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(IRegion.RegionType.COLORBOX.getDefaultColor());	
		setAlpha(80);
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-colorbox.png";
	}
	@Override
	public RegionType getRegionType() {
		return RegionType.COLORBOX;
	}	
	protected Figure createRegionFillFigure() {
		return new RegionFillFigure(this) {
			@Override
			public void paintFigure(Graphics gc) {
				
				super.paintFigure(gc);
				final Rectangle size = getRectangleFromVertices();
				this.bounds = size.getCopy().expand(5, 5);
				gc.setAlpha(getAlpha());
				gc.fillRectangle(size);
				ColoredBoxSelection.this.drawLabel(gc, size);
				drawColoredEdges(gc, size);
			}
		};
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
	public ROIBase createROI(boolean recordResult) {
		if (p1!=null) {
			final Rectangle  rect = getRectangleFromVertices();			
			final RectangularROI    rroi = (RectangularROI)getRoiFromRectangle(rect);
			
			if (recordResult) roi = rroi;
			return rroi;
		}
		return super.getROI();
	}
	
	@Override
	protected RectangularROI createROI(double ptx, double pty, double width, double height, double angle) {
		return new RectangularROI(ptx, pty, width, height, angle);
	}

	
	protected void updateROI(ROIBase roi) {
		if (roi instanceof RectangularROI) {
			RectangularROI rroi = (RectangularROI) roi;
			if (p1!=null) p1.setPosition(rroi.getPointRef());
			if (p4!=null) p4.setPosition(rroi.getEndPoint());
			updateConnectionBounds();
		}
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

	@Override
	protected void updateConnectionBounds() {
		if (connection==null) return;
		final Rectangle size = getRectangleFromVertices();				
		size.expand(5, 5);
		connection.setBounds(size);
	}
	
}
