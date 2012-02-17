package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.Arrays;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.StructuredSelection;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

public class LineSelection extends Region {
		

	private static final int SIDE      = 8;
	
	private SelectionRectangle endBox, startBox;

	private Figure connection;

	public LineSelection(String name, Axis xAxis, Axis yAxis) {
		super(name, xAxis, yAxis);
		setRegionColor(ColorConstants.cyan);
	}


	public void createContents(final Figure parent) {
		
		this.startBox = new SelectionRectangle(getxAxis(), getyAxis(), getRegionColor(), new Point(100,100),  SIDE);
		new FigureMover(getXyGraph(), startBox);	

		this.endBox   = new SelectionRectangle(getxAxis(), getyAxis(), getRegionColor(), new Point(200,200),SIDE);
		new FigureMover(getXyGraph(), endBox);	
				
		this.connection = new RegionFillFigure() {
			@Override
			public void paintFigure(Graphics gc) {
				super.paintFigure(gc);
				final Point startCenter = startBox.getSelectionPoint();
				final Point endCenter   = endBox.getSelectionPoint();
				this.bounds = new Rectangle(startCenter, endCenter);
				gc.setLineWidth(2);
				gc.setAlpha(getAlpha());
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
		
		FigureMover mover = new FigureMover(getXyGraph(), parent, connection, Arrays.asList(new IFigure[]{startBox,endBox}));
		mover.addTranslationListener(new TranslationListener() {
			@Override
			public void translateBefore(TranslationEvent evt) {
			}

			@Override
			public void translationAfter(TranslationEvent evt) {
				final Point startCenter = startBox.getSelectionPoint();
				final Point endCenter   = endBox.getSelectionPoint();
				connection.setBounds(new Rectangle(startCenter, endCenter));
				fireRoiSelection();
			}

		});

		setRegionObjects(connection, startBox, endBox);
		sync(getBean());

	}
	
	protected FigureListener createFigureListener() {
		return new FigureListener() {		
			@Override
			public void figureMoved(IFigure source) {
				
				connection.repaint();
                fireRoiSelection();
			}

		};
	}
	protected void fireRoiSelection() {
		
		// For each trace, calculate the real world values of the selection
		final double[] p1 = startBox.getRealValue();
		final double[] p2 = endBox.getRealValue();
		LinearROI roi = new LinearROI(p1, p2);
		if (getSelectionProvider()!=null) {
			getSelectionProvider().setSelection(new StructuredSelection(roi));
		}
	}
	
	public RegionBounds getBounds() {
		final double[] p1 = startBox.getRealValue();
		final double[] p2 = endBox.getRealValue();
		final RegionBounds bounds = new RegionBounds(p1, p2);
		return bounds;
	}
	
	public void setBounds(RegionBounds bounds) {
		startBox.setRealValue(bounds.getP1());
		endBox.setRealValue(bounds.getP2());
		repaint();
	}

}
