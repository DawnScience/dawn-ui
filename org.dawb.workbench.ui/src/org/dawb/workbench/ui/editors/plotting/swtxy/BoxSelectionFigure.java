package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

public class BoxSelectionFigure extends FixedBoundsShape {
		
	private static final int SIDE      = 8;
	private static final int HALF_SIDE = SIDE/2;
	
	private SelectionRectangle p1,  p2, p3, p4;
	/**
	 *     p1------------p2
	 *     |              |
	 *     |              |
	 *     p3------------p4
	 */

	public BoxSelectionFigure() {
				
		setRequestFocusEnabled(false);
		setFocusTraversable(false);
        setOpaque(false);
        
		this.p1  = createSelectionRectangle(ColorConstants.green,  new Rectangle(10,10,  SIDE, SIDE));
		this.p2  = createSelectionRectangle(ColorConstants.green,  new Rectangle(100,100,SIDE, SIDE));
		this.p3  = createSelectionRectangle(ColorConstants.green,  new Rectangle(100,10, SIDE, SIDE));
		this.p4  = createSelectionRectangle(ColorConstants.green,  new Rectangle(10,100, SIDE, SIDE));
				
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
		connection.setBounds(new Rectangle(Draw2DUtils.getCenter(p4), Draw2DUtils.getCenter(p1)));
		connection.setOpaque(false);
		new FigureMover(connection, this);
		
        add(connection);
		add(p1);
		add(p2);
		add(p3);
		add(p4);
		
		setVisible(false);
		setActive(false);
	}
 	
	private boolean isCalculateCorners = true;

	private SelectionRectangle createSelectionRectangle(Color color, Rectangle rectangle) {
		
		SelectionRectangle rect = new SelectionRectangle(color,  rectangle);
		
		rect.addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
				if (!isCalculateCorners) return;
	
				try {
					isCalculateCorners = false;
					Rectangle sa=null;
					if (source == p1 || source == p4) {
						final Point loc1   = Draw2DUtils.getCenter(p1);
						final Point loc4   = Draw2DUtils.getCenter(p4);
						sa = new Rectangle(loc1, loc4);
						
						boolean quad1Or4 = ((loc1.x<loc4.x && loc1.y<loc4.y) || (loc1.x>loc4.x&&loc1.y>loc4.y));
						setCornerLocation(p2, p3, sa, quad1Or4);
						
					} else if (source == p2 || source == p3) {
						final Point loc2   = Draw2DUtils.getCenter(p2);
						final Point loc3   = Draw2DUtils.getCenter(p3);
						sa = new Rectangle(loc2, loc3);
						
						boolean quad2Or3 = ((loc2.x>loc3.x && loc2.y<loc3.y) || (loc2.x<loc3.x&&loc2.y>loc3.y));
						setCornerLocation(p1, p4, sa, !quad2Or3);
					}

				} finally {
					isCalculateCorners = true;
				}
			}

		});
		return rect;
	}
	
	protected void primTranslate(int dx, int dy) {

		try {
			isCalculateCorners = false;
			super.primTranslate(dx, dy);
		} finally {
			isCalculateCorners = true;
		}
	}

	private void setCornerLocation(SelectionRectangle pnt1,
									SelectionRectangle pnt2, 
									Rectangle sa, 
									boolean quad1Or4) {
		if (quad1Or4) {
			pnt1.setLocation(new Point(sa.x+sa.width-HALF_SIDE-1, sa.y-HALF_SIDE));
			pnt2.setLocation(new Point(sa.x-HALF_SIDE, sa.y+sa.height-HALF_SIDE-1));
		} else {
			pnt1.setLocation(new Point(sa.x-HALF_SIDE, sa.y-HALF_SIDE));
			pnt2.setLocation(new Point(sa.x+sa.width-HALF_SIDE-1, sa.y+sa.height-HALF_SIDE-1));
		}			
	}

	protected Rectangle getRectangleFromVertices() {
		final Point loc1   = Draw2DUtils.getCenter(p1);
		final Point loc2   = Draw2DUtils.getCenter(p2);
		final Point loc3   = Draw2DUtils.getCenter(p3);
		final Point loc4   = Draw2DUtils.getCenter(p4);
		Rectangle size = new Rectangle(loc1, loc4);
		size = size.getUnion(loc2);
		size = size.getUnion(loc3);
		return size;
	}

	protected void notifyStart(MouseEvent me) {
		
		try {
			isCalculateCorners = false;
			final Point loc = me.getLocation();
			p1.setBounds(new Rectangle(loc.x-HALF_SIDE, loc.y-HALF_SIDE, SIDE, SIDE));
			p2.setBounds(new Rectangle(loc.x-HALF_SIDE, loc.y-HALF_SIDE, SIDE, SIDE));
			p3.setBounds(new Rectangle(loc.x-HALF_SIDE, loc.y-HALF_SIDE, SIDE, SIDE));
			p4.setBounds(new Rectangle(loc.x-HALF_SIDE, loc.y-HALF_SIDE, SIDE, SIDE));
			p4.setEnabled(true);
			p4.startMoving(me);
		} finally {
			isCalculateCorners = true;
		}
	}

}
