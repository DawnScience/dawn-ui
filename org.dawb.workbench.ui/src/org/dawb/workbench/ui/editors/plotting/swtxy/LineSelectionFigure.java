package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.Trace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

public class LineSelectionFigure extends RegionShape {
		
	private static final int SIDE      = 8;
	
	private SelectionRectangle endBox, startBox;

	public LineSelectionFigure(final String name, final Trace trace) {
				
		super(name, trace);
		setRequestFocusEnabled(false);
		setFocusTraversable(false);
        setOpaque(false);
        
		this.startBox = new SelectionRectangle(trace, ColorConstants.cyan, new Point(10,10),SIDE);
		this.endBox   = new SelectionRectangle(trace, ColorConstants.cyan, new Point(100,100),SIDE);
				
		final Shape connection = new Shape() {
			protected void outlineShape(Graphics gc) {
				final Point startCenter = startBox.getSelectionPoint();
				final Point endCenter   = endBox.getSelectionPoint();
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
		
		add(startBox);
		add(endBox);
        add(connection);
		
		setVisible(true);
		
		final FigureListener figListener = createFigureListener();
		addFigureListener(figListener);
		startBox.addFigureListener(figListener);
		endBox.addFigureListener(figListener);
	}
	
	public void setShowPosition(boolean showPosition) {
		startBox.setShowPosition(showPosition);
		endBox.setShowPosition(showPosition);
		super.setShowPosition(showPosition);
	}

	
	protected FigureListener createFigureListener() {
		return new FigureListener() {		
			@Override
			public void figureMoved(IFigure source) {
				
				// For each trace, calculate the real world values of the selection
				final double[] p1 = startBox.getRealValue();
				final double[] p2 = endBox.getRealValue();
				LinearROI selection = new LinearROI(p1, p2);

				//TODO 
			}

		};
	}
	
}
