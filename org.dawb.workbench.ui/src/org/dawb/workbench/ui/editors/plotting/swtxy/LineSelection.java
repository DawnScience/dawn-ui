package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.Arrays;

import org.csstudio.swt.xygraph.figures.Trace;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

public class LineSelection extends Region {
		

	private static final int SIDE      = 8;
	
	private SelectionRectangle endBox, startBox;

	private Figure connection;

	private Figure parent;

	public LineSelection(String name, Trace trace) {
		super(name, trace);
	}


	public void createContents(final Figure parent) {
		
		this.parent = parent;
		this.startBox = new SelectionRectangle(trace, getRegionColor(), new Point(100,100),  SIDE);
		new FigureMover(trace.getXYGraph(), startBox);	

		this.endBox   = new SelectionRectangle(trace, getRegionColor(), new Point(200,200),SIDE);
		new FigureMover(trace.getXYGraph(), endBox);	
				
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
		connection.setForegroundColor(getRegionColor());
		connection.setBounds(new Rectangle(startBox.getSelectionPoint(), endBox.getSelectionPoint()));
		connection.setOpaque(false);
		
		parent.add(connection);
		parent.add(startBox);
		parent.add(endBox);
	
				
		final FigureListener figListener = createFigureListener();
		startBox.addFigureListener(figListener);
		endBox.addFigureListener(figListener);
		
		new FigureMover(trace.getXYGraph(), parent, connection, Arrays.asList(new IFigure[]{startBox,endBox}));

		startBox.setShowPosition(isShowPosition());
		endBox.setShowPosition(isShowPosition());

	}
	
	public void remove() {
		parent.remove(connection);
		parent.remove(startBox);
		parent.remove(endBox);
	}

	
	public void setShowPosition(boolean showPosition) {
		if (startBox!=null) startBox.setShowPosition(showPosition);
		if (endBox!=null)   endBox.setShowPosition(showPosition);
		super.setShowPosition(showPosition);
	}

	public void setTrace(Trace trace) {
		if (startBox!=null) startBox.setTrace(trace);
		if (endBox!=null) endBox.setTrace(trace);
		super.setTrace(trace);
	}

	public void setRegionColor(Color color) {
		if (startBox!=null) startBox.setColor(color);
		if (endBox!=null) endBox.setColor(color);
		if (connection!=null) connection.setBackgroundColor(color);
		if (connection!=null) connection.setForegroundColor(color);
		super.setRegionColor(color);
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
