package org.dawb.workbench.plotting.system.swtxy;

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
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

/**
 * 
 *    startBox-----------------------endBox
 * 
 * @author fcp94556
 *
 */
class LineSelection extends Region {
		

	private static final int SIDE      = 8;
	
	private SelectionRectangle endBox, startBox;

	private Figure connection;

	LineSelection(String name, Axis xAxis, Axis yAxis) {
		super(name, xAxis, yAxis);
		setRegionColor(ColorConstants.cyan);
		setAlpha(80);
		setLineWidth(2);
	}


	public void createContents(final Figure parent) {
		
		
		this.startBox = new SelectionRectangle(getxAxis(), getyAxis(), getRegionColor(), new Point(100,100),  SIDE);
		FigureMover mover = new FigureMover(getXyGraph(), startBox);
		mover.addTranslationListener(createRegionNotifier());

		this.endBox   = new SelectionRectangle(getxAxis(), getyAxis(), getRegionColor(), new Point(200,200),SIDE);
		mover = new FigureMover(getXyGraph(), endBox);	
		mover.addTranslationListener(createRegionNotifier());
				
		this.connection = new RegionFillFigure() {
			@Override
			public void paintFigure(Graphics gc) {
				super.paintFigure(gc);
				final Point startCenter = startBox.getSelectionPoint();
				final Point endCenter   = endBox.getSelectionPoint();
				this.bounds = new Rectangle(startCenter, endCenter);
				gc.setLineWidth(getLineWidth());
				gc.setAlpha(getAlpha());
				gc.drawLine(startCenter, endCenter);
				
				LineSelection.this.drawLabel(gc, bounds);
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
		
		mover = new FigureMover(getXyGraph(), parent, connection, Arrays.asList(new IFigure[]{startBox,endBox}));
		mover.addTranslationListener(new TranslationListener() {
			@Override
			public void translateBefore(TranslationEvent evt) {
			}

			@Override
			public void translationAfter(TranslationEvent evt) {
				updateConnectionBounds();
				fireRegionBoundsDragged(createRegionBounds(false));
			}

			@Override
			public void translationCompleted(TranslationEvent evt) {
				fireRegionBoundsChanged(createRegionBounds(true));
				fireRoiSelection();
			}
		});

		setRegionObjects(connection, startBox, endBox);
		sync(getBean());
        updateRegionBounds();
        if (regionBounds==null) createRegionBounds(true);
	}
	
	public void paintBeforeAdded(final Graphics gc, Point firstClick, Point dragLocation, Rectangle parentBounds) {
		gc.setLineStyle(SWT.LINE_DOT);
		gc.setLineWidth(2);
		gc.setAlpha(getAlpha());
		gc.drawLine(firstClick, dragLocation);
	}

	
	protected String getCursorPath() {
		return "icons/Cursor-line.png";
	}

	protected FigureListener createFigureListener() {
		return new FigureListener() {		
			@Override
			public void figureMoved(IFigure source) {				
				connection.repaint();
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
	
	public RegionBounds createRegionBounds(boolean recordResult) {
		if (startBox!=null) {
			final double[] p1 = startBox.getRealValue();
			final double[] p2 = endBox.getRealValue();
			final RegionBounds bounds = new RegionBounds(p1, p2);
			if (recordResult) this.regionBounds = bounds;
			return bounds;
		} 
		return super.getRegionBounds();
	}
	
	protected void updateRegionBounds(RegionBounds bounds) {
		
		if (startBox!=null)   startBox.setRealValue(bounds.getP1());
		if (endBox!=null)     endBox.setRealValue(bounds.getP2());
		updateConnectionBounds();
	}
	
	protected void updateConnectionBounds() {
		if (connection==null) return;
		final Point startCenter = startBox.getSelectionPoint();
		final Point endCenter   = endBox.getSelectionPoint();
		connection.setBounds(new Rectangle(startCenter, endCenter));
	}
	
	/**
	 * Sets the local in local coordinates
	 * @param bounds
	 */
	public void setLocalBounds(Point firstClick, Point dragLocation, Rectangle parentBounds) {
		if (startBox!=null)   startBox.setSelectionPoint(firstClick);
		if (endBox!=null)     endBox.setSelectionPoint(dragLocation);
		updateConnectionBounds();
		createRegionBounds(true);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.LINE;
	}

}
