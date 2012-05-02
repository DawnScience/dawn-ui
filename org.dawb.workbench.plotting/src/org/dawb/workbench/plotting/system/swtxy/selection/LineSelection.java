package org.dawb.workbench.plotting.system.swtxy.selection;

import java.util.Arrays;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Geometry;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

/**
 * 
 *    startBox-----------------------endBox
 * 
 * @author fcp94556
 *
 */
class LineSelection extends AbstractSelectionRegion {

	private static final int SIDE      = 8;
	
	private SelectionHandle endBox, startBox;

	private Figure connection;

	LineSelection(String name, Axis xAxis, Axis yAxis) {
		super(name, xAxis, yAxis);
		setRegionColor(ColorConstants.cyan);
		setAlpha(80);
		setLineWidth(2);
	}

	@Override
	public void createContents(final Figure parent) {
		this.startBox = new RectangularHandle(getXAxis(), getYAxis(), getRegionColor(), connection, SIDE, 100, 100);
		FigureTranslator mover = new FigureTranslator(getXyGraph(), startBox);
		mover.addTranslationListener(createRegionNotifier());

		this.endBox = new RectangularHandle(getXAxis(), getYAxis(), getRegionColor(), connection, SIDE, 200, 200);
		mover = new FigureTranslator(getXyGraph(), endBox);	
		mover.addTranslationListener(createRegionNotifier());
				
		this.connection = new RegionFillFigure(this) {
			PointList shape = new PointList(2);

			@Override
			public void paintFigure(Graphics gc) {
				super.paintFigure(gc);
				final Point startCenter = startBox.getSelectionPoint();
				final Point endCenter   = endBox.getSelectionPoint();
				if (shape.size() == 0) {
					shape.addPoint(startCenter);
					shape.addPoint(endCenter);
				} else {
					shape.setPoint(startCenter, 0);
					shape.setPoint(endCenter, 1);
				}

				this.bounds = getConnectionBounds();
				gc.setLineWidth(getLineWidth());
				gc.setAlpha(getAlpha());
				gc.drawLine(startCenter, endCenter);
				LineSelection.this.drawLabel(gc, bounds);
			}

			@Override
			public boolean containsPoint(int x, int y) {
				if (!super.containsPoint(x, y)) return false;
				return Geometry.polylineContainsPoint(shape, x, y, 2);
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
		
		mover = new FigureTranslator(getXyGraph(), parent, connection, Arrays.asList(new IFigure[]{startBox,endBox}));
		mover.addTranslationListener(createRegionNotifier());

		setRegionObjects(connection, startBox, endBox);
		sync(getBean());
        updateROI();
        if (roi == null) createROI(true);
	}
	
	@Override
	public boolean containsPoint(double x, double y) {
		
		final int xpix = getXAxis().getValuePosition(x, false);
		final int ypix = getYAxis().getValuePosition(y, false);
		return connection.containsPoint(xpix, ypix);
	}


	@Override
	public void paintBeforeAdded(final Graphics gc, PointList clicks, Rectangle parentBounds) {
		gc.setLineStyle(SWT.LINE_DOT);
		gc.setLineWidth(2);
		gc.setAlpha(getAlpha());
		gc.drawLine(clicks.getFirstPoint(), clicks.getLastPoint());
	}

	@Override
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

	@Override
	public ROIBase createROI(boolean recordResult) {
		if (startBox != null) {
			final double[] p1 = startBox.getPosition();
			final double[] p2 = endBox.getPosition();
			final LinearROI lroi = new LinearROI(p1, p2);
			if (recordResult)
				roi = lroi;
			return lroi;
		}
		return super.getROI();
	}
	
	@Override
	protected void updateROI(ROIBase roi) {
		if (roi instanceof LinearROI) {
			LinearROI lroi = (LinearROI) roi;
			if (startBox != null)
				startBox.setPosition(lroi.getPoint());
			if (endBox != null)
				endBox.setPosition(lroi.getEndPoint());
			updateConnectionBounds();
		}
	}
	
	@Override
	protected void updateConnectionBounds() {
		if (connection==null) return;
		final Rectangle bounds = getConnectionBounds();
		connection.setBounds(bounds);
	}
	
	private static final int MIN_BOUNDS = 5;
	/**
	 * Ensures that very thin line does not stop connection moving
	 * @return
	 */
	private Rectangle getConnectionBounds() {
		final Point startCenter = startBox.getSelectionPoint();
		final Point endCenter   = endBox.getSelectionPoint();
		final Rectangle bounds  = new Rectangle(startCenter, endCenter);
		if (bounds.height<MIN_BOUNDS) bounds.height=MIN_BOUNDS;
		if (bounds.width <MIN_BOUNDS) bounds.width=MIN_BOUNDS;
		return bounds;
	}

	/**
	 * Sets the local in local coordinates
	 * @param bounds
	 */
	@Override
	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
		if (startBox!=null)   startBox.setSelectionPoint(clicks.getFirstPoint());
		if (endBox!=null)     endBox.setSelectionPoint(clicks.getLastPoint());
		updateConnectionBounds();
		createROI(true);
		fireROIChanged(getROI());
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.LINE;
	}

	@Override
	public int getMaximumMousePresses() {
		return 2;
	}
}
