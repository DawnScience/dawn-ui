package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.Trace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

public class BoxSelectionFigure extends RegionShape {
		
	private static final int SIDE      = 8;
	private static final int HALF_SIDE = SIDE/2;
	
	private SelectionRectangle p1,  p2, p3, p4;
	
	/**
	 *     p1------------p2
	 *     |              |
	 *     |              |
	 *     p3------------p4
	 */

	public BoxSelectionFigure(String name, Trace trace) {
		
		super(name, trace);
		
		setRequestFocusEnabled(false);
		setFocusTraversable(false);
        setOpaque(false);
        
		this.p1  = createSelectionRectangle(ColorConstants.green,  new Point(10,10), SIDE);
		this.p2  = createSelectionRectangle(ColorConstants.green,  new Point(100,10), SIDE);
		this.p3  = createSelectionRectangle(ColorConstants.green,  new Point(10,100), SIDE);
		this.p4  = createSelectionRectangle(ColorConstants.green,  new Point(100,100), SIDE);
				
		final Shape connection = new Shape() {
			protected void outlineShape(Graphics gc) {
			}
			@Override
			protected void fillShape(Graphics gc) {
                final Rectangle size = getRectangleFromVertices();				
				gc.fillRectangle(size);
				setBounds(size);
				
			}
		};
		connection.setCursor(Draw2DUtils.getRoiMoveCursor());
		connection.setLineWidth(2);
		connection.setAlpha(80);
		connection.setBackgroundColor(ColorConstants.green);
		connection.setBounds(new Rectangle(p4.getSelectionPoint(), p1.getSelectionPoint()));
		connection.setOpaque(false);
		connection.setRequestFocusEnabled(false);
		connection.setFocusTraversable(false);
     	new FigureMover(connection, this);
		
        add(connection);
		add(p1);
		add(p2);
		add(p3);
		add(p4);
		
		setVisible(true);
	}
	
	public void setShowPosition(boolean showPosition) {
		p1.setShowPosition(showPosition);
		p2.setShowPosition(showPosition);
		p3.setShowPosition(showPosition);
		p4.setShowPosition(showPosition);
		super.setShowPosition(showPosition);
	}
	
	private boolean isCalculateCorners = true;

	private SelectionRectangle createSelectionRectangle(Color color, Point location, int size) {
		
		SelectionRectangle rect = new SelectionRectangle(trace, color,  location, size);
		
		rect.addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
				if (!isCalculateCorners) return;
	
				try {
					isCalculateCorners = false;
					SelectionRectangle a=null, b=null, c=null, d=null;
					if (source == p1 || source == p4) {
						a   = p1;   b   = p4;   c   = p2;   d   = p3;
	
					} else if (source == p2 || source == p3) {
						a   = p2;   b   = p3;   c   = p1;   d   = p4;
					} else {
						return;
					}
				
					Point pa   = a.getSelectionPoint();
					Point pb   = b.getSelectionPoint();
					Rectangle sa = new Rectangle(pa, pb);
					
					boolean quad1Or4 = ((pa.x<pb.x && pa.y<pb.y) || (pa.x>pb.x&&pa.y>pb.y));
					setCornerLocation(c, d, sa, quad1Or4);


				} finally {
					isCalculateCorners = true;
				}
			}

		});
		return rect;
	}
	
	private void setCornerLocation( SelectionRectangle c,
									SelectionRectangle d, 
									Rectangle sa, 
									boolean quad1Or4) {
		if (quad1Or4) {
			c.setSelectionPoint(new Point(sa.x+sa.width, sa.y));
			d.setSelectionPoint(new Point(sa.x, sa.y+sa.height));
		} else {
			c.setSelectionPoint(new Point(sa.x, sa.y));
			d.setSelectionPoint(new Point(sa.x+sa.width, sa.y+sa.height));
		}			
	}

	protected void primTranslate(int dx, int dy) {

		try {
			isCalculateCorners = false;
			super.primTranslate(dx, dy);
		} finally {
			isCalculateCorners = true;
		}
	}


	protected Rectangle getRectangleFromVertices() {
		final Point loc1   = p1.getSelectionPoint();
		final Point loc4   = p4.getSelectionPoint();
		Rectangle size = new Rectangle(loc1, loc4);
		return size;
	}

}
