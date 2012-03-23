package org.dawb.workbench.plotting.system.swtxy;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.csstudio.swt.xygraph.figures.Axis;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

class SelectionRectangle extends Figure implements IMobileFigure {
	
	private RectangleFigure point;
	private Figure          label;
	protected Axis          xAxis;
	protected Axis          yAxis;
	private int             alpha=100;

	SelectionRectangle(Axis xAxis, Axis yAxis, Color colour, Point location, int side) {
		
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		setOpaque(false);
		
		this.label = new Figure(){
			protected void paintFigure(Graphics graphics) {
				if (!isVisible()) return;
                final String text = getLabelPositionText(getRealValue());
                graphics.setForegroundColor(ColorConstants.black);
                graphics.drawString(text, getLocation());
                super.paintFigure(graphics);
			}
		};
		label.setBounds(new Rectangle(0,side,200,20));
		label.setLocation(new Point(0,0));
		label.setVisible(false);
		add(label);

		this.point = new RectangleFigure();
		point.setAlpha(alpha);
		point.setBackgroundColor(colour);
		point.setForegroundColor(colour);
		point.setOpaque(false);
		point.setBounds(new Rectangle(0,0,side,side));
		point.setCursor(Draw2DUtils.getRoiControlPointCursor());
		add(point);
		
		
        setBounds(new Rectangle(location, new Point(location.x+200, location.y+20)));
		
 	}

	/**
	 * Gets the position in the scale of the axis.
	 * @return
	 */
	public double[] getRealValue() {
		final Point p = getSelectionPoint();
		try {
		    return new double[]{xAxis.getPositionValue(p.x, false), yAxis.getPositionValue(p.y, false)};
		} catch (NullPointerException ne) {
			return new double[]{Double.NaN, Double.NaN};
		}
	}
	
	/**
	 * Sets the location in the scale of the axis. Transforms this point 
	 * to the sceen value and then relocates the point.
	 * 
	 * @param point
	 */
	public void setRealValue(final double[] point) {
		
		final Point pnt = new Point(xAxis.getValuePosition(point[0], false), yAxis.getValuePosition(point[1], false));
		setSelectionPoint(pnt);
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

	public Point getSelectionPoint() {
		final Point loc = getLocation();
		final int pntWid = point.getBounds().width;
		final int pntHgt = point.getBounds().height;
		return new Point(loc.x+(pntWid/2), loc.y+(pntHgt/2));
	}

	protected void setSelectionPoint(Point p) {
		final int pntWid = point.getBounds().width;
		final int pntHgt = point.getBounds().height;
		final Point loc = new Point(p.x-(pntWid/2), p.y-(pntHgt/2));
		setLocation(loc);
		point.setLocation(loc);
	}

	public void setShowPosition(boolean showPosition) {
		label.setVisible(showPosition);
	}

	public void setxAxis(Axis axis) {
		this.xAxis = axis;
		repaint();
	}
	public void setyAxis(Axis axis) {
		this.yAxis = axis;
		repaint();
	}

	public void setForegroundColor(Color fg) {
		point.setForegroundColor(fg);
		super.setForegroundColor(fg);
	}

	public void setBackgroundColor(Color bg) {
		point.setBackgroundColor(bg);
		super.setBackgroundColor(bg);
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
		point.setAlpha(alpha);
	}

}