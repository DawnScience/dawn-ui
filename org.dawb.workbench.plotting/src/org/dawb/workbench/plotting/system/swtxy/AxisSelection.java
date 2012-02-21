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
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;


/**
 *     Either
 *     |   |
 *     |   |
 *     
 *     or
 *     ------------
 *     ------------
 *     
 * @author fcp94556
 */
class AxisSelection extends Region {
		
	private static final int WIDTH = 8;
	
	private Figure line1, line2;
	private Figure connection;

	private RegionType regionType;
	
	AxisSelection(String name, Axis xAxis, Axis yAxis, RegionType regionType) {
		super(name, xAxis, yAxis);
		if (regionType!=RegionType.XAXIS && regionType!=RegionType.YAXIS) {
			throw new RuntimeException("The AxisSelection can only be XAXIS or YAXIS region type!");
		}
		setRegionColor(ColorConstants.blue);	
		setAlpha(80);
		this.regionType = regionType;
	}

	public void createContents(final Figure parent) {
		
     	this.line1  = createLineFigure(true,  parent.getBounds());
    	this.line2  = createLineFigure(false, parent.getBounds());
 				
		this.connection = new RegionFillFigure() {
			@Override
			public void paintFigure(Graphics gc) {
				super.paintFigure(gc);
				final Rectangle size = getRectangleFromVertices();				
				this.bounds = size;
				gc.setAlpha(getAlpha());
				gc.fillRectangle(size);
				
				AxisSelection.this.drawLabel(gc, size);
			}
		};
		connection.setCursor(Draw2DUtils.getRoiMoveCursor());
		connection.setBackgroundColor(getRegionColor());
		connection.setBounds(new Rectangle(line1.getLocation(), line2.getBounds().getBottomRight()));
		connection.setOpaque(false);
  		
		parent.add(connection);
		parent.add(line1);
		parent.add(line2);
				
		FigureMover mover = new FigureMover(getXyGraph(), parent, connection, Arrays.asList(new IFigure[]{line1, line2}));
		if (regionType==RegionType.XAXIS) {
			mover.setLockedDirection(FigureMover.LockType.X);
		} else {
			mover.setLockedDirection(FigureMover.LockType.Y);
		}
		// Add a translation listener to be notified when the mover will translate so that
		// we do not recompute point locations during the move.
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
		

	   	setRegionObjects(connection, line1, line2);
		sync(getBean());
        updateRegionBounds();
        if (regionBounds==null) createRegionBounds(true);

	}
	
	public void paintBeforeAdded(final Graphics gc, Point firstClick, Point dragLocation, Rectangle parentBounds) {
		
		
		final Rectangle bounds = new Rectangle(firstClick, dragLocation);
		Rectangle r  = getSelectionBounds(bounds, parentBounds);
		
		
		gc.setLineStyle(SWT.LINE_DOT);
     	if (regionType==RegionType.XAXIS) {
     		r.width+=2;
			gc.drawLine(r.getBottomRight(), r.getTopRight());
			gc.drawLine(r.getBottomLeft(),  r.getTopLeft());
		} else {
			r.height+=2;
			gc.drawLine(r.getTopLeft(),     r.getTopRight());
			gc.drawLine(r.getBottomLeft(),  r.getBottomRight());
		}
		gc.setBackgroundColor(getRegionColor());
		gc.setAlpha(getAlpha());
		gc.fillRectangle(r);
	}

	
	private Rectangle getSelectionBounds(Rectangle bounds,
			                             Rectangle parentBounds) {
		
		Rectangle r;
		if (regionType==RegionType.XAXIS) { // Draw vertical lines
			r = new Rectangle(new Point(bounds.getLocation().x,    parentBounds.getLocation().y), 
					          new Point(bounds.getBottomRight().x, parentBounds.getBottomRight().y));
		
		} else { // Draw horizontal lines
			r = new Rectangle(new Point(parentBounds.getLocation().x,    bounds.getLocation().y), 
                              new Point(parentBounds.getBottomRight().x, bounds.getBottomRight().y));
			
		}
		return r;
	}

	protected String getCursorPath() {
		if (regionType==RegionType.XAXIS) {
			return "icons/Cursor-horiz.png";
		} else {
			return "icons/Cursor-vert.png";
		}
	}
	

	private Figure createLineFigure(final boolean first, Rectangle parent) {
		
		Figure line = new Figure() {
			protected void paintFigure(Graphics gc) {
				
				super.paintFigure(gc);
				gc.setLineStyle(SWT.LINE_DOT);
				final Rectangle b = getBounds();
				if (regionType==RegionType.XAXIS) {
					if (first) {
					    gc.drawLine(b.getTopLeft(), b.getBottomLeft());
					} else {
						gc.drawLine(b.getTopRight().translate(-1, 0), b.getBottomRight().translate(-1, 0));
					}
				} else {
					if (first) {
					    gc.drawLine(b.getTopLeft(), b.getTopRight());
					} else {
					    gc.drawLine(b.getBottomLeft().translate(0,1), b.getBottomRight().translate(0,1));
					}
				}
			}
		};
		
		//line.setBorder(new LineBorder());
		
		if (regionType==RegionType.XAXIS) {
			line.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEWE));
			line.setBounds(new Rectangle(parent.x, parent.y, WIDTH, parent.height));
		} else {
			line.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZENS));
			line.setBounds(new Rectangle(parent.x, parent.y, parent.width, WIDTH));
		}
		
		FigureMover mover = new FigureMover(getXyGraph(), line);
		if (regionType==RegionType.XAXIS) {
			mover.setLockedDirection(FigureMover.LockType.X);
		} else {
			mover.setLockedDirection(FigureMover.LockType.Y);
		}
		mover.addTranslationListener(createRegionNotifier());
		line.addFigureListener(createFigureListener());
		
		return line;
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
		final RegionBounds bounds = createRegionBounds(false);
		final RectangularROI roi = new RectangularROI(bounds.getP1()[0], bounds.getP1()[1], bounds.getP1()[0]+bounds.getP2()[0], bounds.getP1()[1]+bounds.getP2()[1], 0);
		if (getSelectionProvider()!=null) getSelectionProvider().setSelection(new StructuredSelection(roi));
	}


	protected Rectangle getRectangleFromVertices() {
		final Point loc1   = line1.getLocation();
		final Point loc4   = line2.getBounds().getBottomRight();
		Rectangle size = new Rectangle(loc1, loc4);
		return size;
	}


	public RegionBounds createRegionBounds(boolean recordResult) {
		if (line1!=null) {
			final Rectangle rect = getRectangleFromVertices();
			double[] a1 = new double[]{getxAxis().getPositionValue(rect.x, false), getyAxis().getPositionValue(rect.y, false)};
			double[] a2 = new double[]{getxAxis().getPositionValue(rect.x+rect.width, false), getyAxis().getPositionValue(rect.y+rect.height, false)};
			final RegionBounds bounds = new RegionBounds(a1, a2);
			if (recordResult) this.regionBounds = bounds;
			return bounds;
		}
		return super.getRegionBounds();
	}
	
	protected void updateRegionBounds(RegionBounds bounds) {
		
		if (line1!=null && line2!=null) {
			final Point     p1    = new Point(getxAxis().getValuePosition(bounds.getP1()[0], false),
					                          getyAxis().getValuePosition(bounds.getP1()[1], false));
			final Point     p2    = new Point(getxAxis().getValuePosition(bounds.getP2()[0], false),
                                              getyAxis().getValuePosition(bounds.getP2()[1], false));
			
			final Rectangle local = new Rectangle(p1, p2);
			setLocalBounds(local, line1.getParent().getBounds());
			updateConnectionBounds();
		}
	}
	/**
	 * Sets the local in local coordinates
	 * @param bounds
	 */
	public void setLocalBounds(Point firstClick, Point dragLocation, Rectangle parentBounds) {
		if (line1!=null) {
			setLocalBounds(new Rectangle(firstClick, dragLocation), parentBounds);
			createRegionBounds(true);
		}
	}

	protected void setLocalBounds(Rectangle box, Rectangle parentBounds) {
		if (line1!=null && line2!=null) {
			final Rectangle bounds = getSelectionBounds(box, parentBounds);
			
			if (regionType==RegionType.XAXIS) {
				line1.setLocation(bounds.getTopLeft());
				line2.setLocation(new Point(bounds.getTopRight().x-WIDTH, bounds.getTopRight().y));
			} else {
				line1.setLocation(bounds.getTopLeft());
				line2.setLocation(new Point(bounds.getBottomLeft().x, bounds.getBottomLeft().y-WIDTH));
			}
		}
		updateConnectionBounds();
		
	}

	protected void updateConnectionBounds() {
		if (connection==null) return;
		final Rectangle size = getRectangleFromVertices();				
		connection.setBounds(size);
	}
	
	@Override
	public RegionType getRegionType() {
		return regionType;
	}

}
