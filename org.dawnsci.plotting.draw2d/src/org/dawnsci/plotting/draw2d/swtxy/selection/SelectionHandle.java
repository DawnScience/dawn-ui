package org.dawnsci.plotting.draw2d.swtxy.selection;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.draw2d.swtxy.IMobileFigure;
import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
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
	protected ICoordinateSystem   coords;
	private int             alpha=100;
	protected Point location;

	protected SelectionHandle(ICoordinateSystem coords, Color colour, Figure parent, int side, double... params) {
		this.coords = coords;
		setOpaque(false);

		this.label = new Figure() {
			@Override
			protected void paintFigure(Graphics graphics) {
				if (!isVisible()) return;
                final String text = getLabelPositionText(getPosition());
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
	
	public void setVisible(boolean visible) {
		shape.setVisible(visible);
		super.setVisible(visible);
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
	public double[] getPosition() {
		if (coords != null) {
			final Point p = getSelectionPoint();
			double[] point = coords.getPositionValue(new int[] { p.x, p.y });
			return point;
		}
	    return new double[]{Double.NaN, Double.NaN};
	}
	
	/**
	 * Sets the location in the scale of the axis. Transforms this point 
	 * to the screen value and then relocates the point.
	 * 
	 * @param point
	 */
	public void setPosition(final double[] point) {
		final int[] val = coords.getValuePosition(point);
		final Point pnt = new Point(val[0], val[1]);
		setSelectionPoint(pnt);
	}

	private NumberFormat format = new DecimalFormat("######0.00");

	protected String getLabelPositionText(double[] p) {
		if (Double.isNaN(p[0])||Double.isNaN(p[1])) return "";
		return String.format("(%s,%s)", format.format(p[0]), format.format(p[1]));
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

	public void setCoordinateSystem(ICoordinateSystem axes) {
		this.coords = axes;
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

	/**
	 * Remove all mouse listeners
	 */
	public void removeMouseListeners() {
		Iterator<?> it = getListeners(MouseListener.class);
		while (it.hasNext()) {
			removeListener(MouseListener.class, it.next());
		}

		it = getListeners(MouseMotionListener.class);
		while (it.hasNext()) {
			removeListener(MouseMotionListener.class, it.next());
		}
	}
}
