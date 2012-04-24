package org.dawb.workbench.plotting.system.swtxy.selection;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.workbench.plotting.system.swtxy.IMobileFigure;
import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;

/**
 * Abstract class for a GUI handle that allows a selection region to be manipulated
 */
public abstract class SelectionHandle extends Figure implements IMobileFigure {
	
	private Shape shape;
	private Figure          label;
	protected Axis          xAxis;
	protected Axis          yAxis;
	private int             alpha=100;
	protected Point location;

	protected SelectionHandle(Axis xAxis, Axis yAxis, Color colour, Figure parent, int side, double... params) {
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		setOpaque(false);
		
		this.label = new Figure() {
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

		shape = createHandleShape(parent, side, params);
		shape.setAlpha(alpha);
		shape.setBackgroundColor(colour);
		shape.setForegroundColor(colour);
		shape.setOpaque(false);
		shape.setCursor(Draw2DUtils.getRoiControlPointCursor());
		add(shape);

		Rectangle b = new Rectangle(location, new Point(location.x()+200, location.y()+20));
		b.union(shape.getBounds());
        setBounds(b);
 	}

	/**
	 * Create a handle shape (and set its own bounds and location)
	 * @param parent shape
	 * @param side
	 * @param params
	 * @return handle shape
	 */
	public abstract Shape createHandleShape(Figure parent, int side, double[] params);

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
	 * to the screen value and then relocates the point.
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
	
	public void setCursor(final Cursor cursor) {
		super.setCursor(cursor);
		this.shape.setCursor(cursor);
	}

	public Point getSelectionPoint() {
		final Point loc = getLocation();
		final int pntWid = shape.getBounds().width;
		final int pntHgt = shape.getBounds().height;
		return new Point(loc.x+(pntWid/2), loc.y+(pntHgt/2));
	}

	protected void setSelectionPoint(Point p) {
		final int pntWid = shape.getBounds().width;
		final int pntHgt = shape.getBounds().height;
		final Point loc = new Point(p.x-(pntWid/2), p.y-(pntHgt/2));
		setLocation(loc);
	}

	@Override
	public void setLocation(Point loc) {
		super.setLocation(loc);
		shape.setLocation(loc);
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
		shape.setForegroundColor(fg);
		super.setForegroundColor(fg);
	}

	public void setBackgroundColor(Color bg) {
		shape.setBackgroundColor(bg);
		super.setBackgroundColor(bg);
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
		shape.setAlpha(alpha);
	}

	@Override
	public boolean containsPoint(int x, int y) {
		return shape.containsPoint(x, y);
	}
}
