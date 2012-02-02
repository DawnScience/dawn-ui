package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class LineSelectionFigure extends FixedBoundsShape {
		
	
	private SelectionRectangle endBox, startBox;

	public LineSelectionFigure() {
				
		setRequestFocusEnabled(false);
		setFocusTraversable(false);
        setOpaque(false);
        
		this.startBox = new SelectionRectangle(ColorConstants.cyan, new Rectangle(10,10,8,8));
		this.endBox   = new SelectionRectangle(ColorConstants.cyan, new Rectangle(100,100,8,8));
				
		final Shape connection = new Shape() {
			protected void outlineShape(Graphics gc) {
				final Point startCenter = Draw2DUtils.getCenter(startBox);
				final Point endCenter   = Draw2DUtils.getCenter(endBox);
				gc.drawLine(startCenter, endCenter);
				setBounds(new Rectangle(startCenter, endCenter));
			}
			@Override
			protected void fillShape(Graphics graphics) {
				// TODO Auto-generated method stub
				
			}
		};
		connection.setCursor(Draw2DUtils.getRoiMoveCursor());
		connection.setLineWidth(2);
		connection.setAlpha(80);
		connection.setForegroundColor(ColorConstants.cyan);
		connection.setBounds(new Rectangle(Draw2DUtils.getCenter(startBox), Draw2DUtils.getCenter(endBox)));
		connection.setOpaque(false);
		new FigureMover(connection, this);
		
        add(connection);
		add(startBox);
		add(endBox);
		
		setVisible(false);
		setActive(false);
	}
 	
	protected void notifyStart(MouseEvent me) {
		final Point loc = me.getLocation();
		startBox.setBounds(new Rectangle(loc.x, loc.y, 8, 8));
		endBox.setBounds(new Rectangle(loc.x, loc.y, 8, 8));
		endBox.setEnabled(true);
		endBox.startMoving(me);
		endBox.handleMouseDragged(me);
	}

}
