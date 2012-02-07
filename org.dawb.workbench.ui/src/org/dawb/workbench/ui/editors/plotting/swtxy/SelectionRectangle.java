package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.csstudio.swt.xygraph.figures.Trace;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

class SelectionRectangle extends Figure {
	
	private FigureMover     mover;
	private RectangleFigure point;
	private Figure          label;
	private Trace trace;

	SelectionRectangle(Trace trace, Color colour, Point location, int side) {
		
		this.trace = trace;
		//setLayoutManager(new GridLayout(1, false));
		setOpaque(false);
		setRequestFocusEnabled(false);
		setFocusTraversable(false);
		
		this.label = new Figure(){
			protected void paintFigure(Graphics graphics) {
                final String text = getLabelPositionText(getRealValue());
                graphics.drawString(text, getLocation());
                super.paintFigure(graphics);
			}
		};
		label.setRequestFocusEnabled(false);
		label.setFocusTraversable(false);
		label.setBounds(new Rectangle(0,side,200,20));
		label.setLocation(new Point(0,0));
		add(label);

		this.point = new RectangleFigure();
		point.setAlpha(100);
		point.setBackgroundColor(colour);
		point.setForegroundColor(colour);
		point.setOpaque(false);
		point.setBounds(new Rectangle(0,0,side,side));
		point.setCursor(Draw2DUtils.getRoiControlPointCursor());
		this.mover = new FigureMover(point, this);	
		add(point);
		
		
        setBounds(new Rectangle(location, new Point(location.x+200, location.y+20)));
		
 		//setBorder(new LineBorder());
	}

	public void startMoving(MouseEvent me) {
		mover.startMoving(me);
	}

	public double[] getRealValue() {
		final Point p = getSelectionPoint();
		return new double[]{trace.getXAxis().getPositionValue(p.x, false), trace.getYAxis().getPositionValue(p.y, false)};
	}
	
	private NumberFormat format = new DecimalFormat("######0.00");
	
	protected String getLabelPositionText(double[] p) {
		if (label==null) return "";
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
		revalidate();
	}

	public void setShowPosition(boolean showPosition) {
		label.setVisible(showPosition);
	}

}