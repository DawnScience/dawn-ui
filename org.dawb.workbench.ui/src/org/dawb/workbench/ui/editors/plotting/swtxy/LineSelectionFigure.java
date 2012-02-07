package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.Trace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

public class LineSelectionFigure extends RegionFigure {
		
	private static final int SIDE      = 8;
	
	private SelectionRectangle endBox, startBox;

	private Figure connection;

	public LineSelectionFigure(final String name, final Trace trace) {
				
		super(name, trace);
        setOpaque(false);
        
		this.startBox = new SelectionRectangle(trace, ColorConstants.cyan, new Point(10,10),  SIDE);
		new FigureMover(trace.getXYGraph(), startBox, false);	

		this.endBox   = new SelectionRectangle(trace, ColorConstants.cyan, new Point(100,100),SIDE);
		new FigureMover(trace.getXYGraph(), endBox, false);	
				
		this.connection = new Figure() {
			@Override
			public void paintFigure(Graphics gc) {
				super.paintFigure(gc);
				final Point startCenter = startBox.getSelectionPoint();
				final Point endCenter   = endBox.getSelectionPoint();
				this.bounds = new Rectangle(startCenter, endCenter);
				gc.setLineWidth(2);
				gc.setAlpha(80);
				gc.drawLine(startCenter, endCenter);
			}
		};
		connection.setCursor(Draw2DUtils.getRoiMoveCursor());
		connection.setForegroundColor(ColorConstants.cyan);
		connection.setBounds(new Rectangle(startBox.getSelectionPoint(), endBox.getSelectionPoint()));
		connection.setOpaque(false);
		
        add(connection);
	    add(startBox);
		add(endBox);
	
		
		setVisible(true);
		
		final FigureListener figListener = createFigureListener();
		startBox.addFigureListener(figListener);
		endBox.addFigureListener(figListener);
		
		new FigureMover(trace.getXYGraph(), this, connection, true);

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
				
				connection.repaint();
				
				// For each trace, calculate the real world values of the selection
				final double[] p1 = startBox.getRealValue();
				final double[] p2 = endBox.getRealValue();
				LinearROI selection = new LinearROI(p1, p2);

				//TODO 
			}

		};
	}
	
}
