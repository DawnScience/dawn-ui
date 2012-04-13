package org.dawb.workbench.plotting.system.swtxy.selection;

import java.util.Arrays;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.workbench.plotting.system.swtxy.IMobileFigure;
import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
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
class AxisSelection extends AbstractSelectionRegion {
		
	private static final int WIDTH = 8;
	
	private LineFigure line1, line2;
	private Figure connection;

	private RegionType regionType;

	private MouseMotionListener mouseTrackListener;
	
	AxisSelection(String name, Axis xAxis, Axis yAxis, RegionType regionType) {
		super(name, xAxis, yAxis);
		if (regionType!=RegionType.XAXIS && regionType!=RegionType.YAXIS && regionType!=RegionType.XAXIS_LINE && regionType!=RegionType.YAXIS_LINE) {
			throw new RuntimeException("The AxisSelection can only be XAXIS or YAXIS region type!");
		}
		setRegionColor(ColorConstants.blue);	
		setAlpha(80);
		setLineWidth(1);
		this.regionType = regionType;
	}

	public void createContents(final Figure parent) {
		
     	this.line1  = new LineFigure(true,  parent.getBounds());
     	
     	if (regionType==RegionType.XAXIS || regionType==RegionType.YAXIS) {
    	    this.line2  = new LineFigure(false, parent.getBounds());
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
    		parent.add(line2);
        }
     	
		parent.add(line1);
				

     	if (regionType==RegionType.XAXIS || regionType==RegionType.YAXIS) {
    		FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, connection, Arrays.asList(new IFigure[]{line1, line2}));
    		
    		if (regionType==RegionType.XAXIS || regionType==RegionType.XAXIS_LINE) {
    			mover.setLockedDirection(FigureTranslator.LockType.X);
    		} else {
    			mover.setLockedDirection(FigureTranslator.LockType.Y);
    		}
    		// Add a translation listener to be notified when the mover will translate so that
    		// we do not recompute point locations during the move.
    		mover.addTranslationListener(createRegionNotifier());
	   	    setRegionObjects(connection, line1, line2);
     	} else {
     		setRegionObjects(line1);
     	}
		sync(getBean());
        updateRegionBounds();
        if (regionBounds==null) createRegionBounds(true);

        
        parent.addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
				if (line1!=null) line1.updateBounds(source.getBounds());
				if (line2!=null) line2.updateBounds(source.getBounds());
				updateConnectionBounds();
			}
		});
        
        this.mouseTrackListener = new MouseMotionListener.Stub() {
        	
        	/**
    		 * @see org.eclipse.draw2d.MouseMotionListener#mouseMoved(MouseEvent)
    		 */
    		public void mouseMoved(MouseEvent me) {
    			Point loc = me.getLocation();
    			if (line1!=null && line1.getParent()!=null) {
	     			if (regionType==RegionType.XAXIS_LINE) {
	        			line1.getBounds().setX(loc.x);
	    			} else if (regionType==RegionType.YAXIS_LINE) {
	           			line1.getBounds().setY(loc.y);
	    			}
	    			line1.getParent().repaint();
	    			
	    			fireRegionBoundsDragged(createRegionBounds(false));
    			}
    		}
        };
        
        setTrackMouse(isTrackMouse());
	}
	
	protected void clearListeners() {
        super.clearListeners();
        if (line1!=null && line1.getParent()!=null && isTrackMouse()) {
        	line1.getParent().removeMouseMotionListener(mouseTrackListener);
        }
	}
	
	public void paintBeforeAdded(final Graphics gc, Point firstClick, Point dragLocation, Rectangle parentBounds) {
		
		
		final Rectangle bounds = new Rectangle(firstClick, dragLocation);
		Rectangle r  = getSelectionBounds(bounds, parentBounds);
		
		
		gc.setLineStyle(SWT.LINE_DOT);
     	if (regionType==RegionType.XAXIS || regionType==RegionType.XAXIS_LINE) {
     		if (regionType==RegionType.XAXIS) r.width+=2;
			gc.drawLine(r.getBottomRight(), r.getTopRight());
			if (regionType==RegionType.XAXIS) gc.drawLine(r.getBottomLeft(),  r.getTopLeft());
		} else {
			if (regionType==RegionType.YAXIS) r.height+=2;
			gc.drawLine(r.getTopLeft(),     r.getTopRight());
			if (regionType==RegionType.YAXIS) gc.drawLine(r.getBottomLeft(),  r.getBottomRight());
		}
		gc.setBackgroundColor(getRegionColor());
		gc.setAlpha(getAlpha());
		gc.fillRectangle(r);
	}

	
	private Rectangle getSelectionBounds(Rectangle bounds,
			                             Rectangle parentBounds) {
		
		Rectangle r;
		if (regionType==RegionType.XAXIS || regionType==RegionType.XAXIS_LINE) { // Draw vertical lines
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
	
	protected final class LineFigure extends Figure implements IMobileFigure {
		
		private boolean first;
		private FigureTranslator mover;
		
		LineFigure(final boolean first, Rectangle parent) {
			this.first = first;
			setOpaque(false);
            updateBounds(parent);			
			this.mover = new FigureTranslator(getXyGraph(), this);
			if (regionType==RegionType.XAXIS || regionType==RegionType.XAXIS_LINE) {
				mover.setLockedDirection(FigureTranslator.LockType.X);
			} else {
				mover.setLockedDirection(FigureTranslator.LockType.Y);
			}
			mover.addTranslationListener(createRegionNotifier());
			addFigureListener(createFigureListener());
			mover.setActive(isMobile());
		}
		protected void updateBounds(Rectangle parentBounds) {
			if (regionType==RegionType.XAXIS|| regionType==RegionType.XAXIS_LINE) {
				if (!isTrackMouse()) setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEWE));
				setBounds(new Rectangle(parentBounds.x, parentBounds.y, getLineAreaWidth(), parentBounds.height));
			} else {
				if (!isTrackMouse()) setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZENS));
				setBounds(new Rectangle(parentBounds.x, parentBounds.y, parentBounds.width, getLineAreaWidth()));
			}
		}
		protected void paintFigure(Graphics gc) {
			
			super.paintFigure(gc);
			
			if (regionType==RegionType.XAXIS || regionType==RegionType.YAXIS) {
				gc.setLineStyle(SWT.LINE_DOT);
			} else {
				gc.setLineStyle(SWT.LINE_SOLID);
			}
			
			gc.setLineWidth(getLineWidth());
			final Rectangle b = getBounds();
			if (regionType==RegionType.XAXIS || regionType==RegionType.XAXIS_LINE) {
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
		public void setMotile(boolean motile) {
			mover.setActive(motile);
		}
	}
	
	public void setMobile(boolean motile) {
		if (regionType==RegionType.XAXIS || regionType==RegionType.YAXIS) {
			super.setMobile(motile);
			return;
		}
		
		if (line1!=null) line1.setMotile(motile);
	}
	
	protected FigureListener createFigureListener() {
		return new FigureListener() {		
			@Override
			public void figureMoved(IFigure source) {				
				if (connection!=null) connection.repaint();
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
		final Point loc4   = line2!=null 
				           ? line2.getBounds().getBottomRight()
				           : line1.getBounds().getBottomRight();
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
		
		if (line1!=null) {
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
			fireRegionBoundsChanged(getRegionBounds());
		}
	}

	protected void setLocalBounds(Rectangle box, Rectangle parentBounds) {
		if (line1!=null) {
			final Rectangle bounds = getSelectionBounds(box, parentBounds);

			if (regionType==RegionType.XAXIS || regionType==RegionType.XAXIS_LINE) {
				line1.setLocation(bounds.getTopLeft());
				if (line2!=null) line2.setLocation(new Point(bounds.getTopRight().x-getLineAreaWidth(), bounds.getTopRight().y));
			} else {
				line1.setLocation(bounds.getTopLeft());
				if (line2!=null) line2.setLocation(new Point(bounds.getBottomLeft().x, bounds.getBottomLeft().y-getLineAreaWidth()));
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

	public void setTrackMouse(boolean track) {
		super.setTrackMouse(track);
       
		if (line1!=null) {
			if (isTrackMouse()) {
				line1.setEnabled(false);// This stops the figure being part of mouse listeners
				line1.getParent().addMouseMotionListener(mouseTrackListener);
	        	line1.getParent().setCursor(Cursors.CROSS);
	        	line1.setCursor(null);
	        	if (connection!=null) connection.setCursor(null);
	        	line1.setMotile(false); // Not moved by the user, moved by the mouse position.
	        } else {
	        	line1.getParent().removeMouseMotionListener(mouseTrackListener);
	        	line1.getParent().setCursor(null);
	        	line1.setEnabled(true);// This starts the figure part of mouse listeners
	        	if (connection!=null) connection.setCursor(Draw2DUtils.getRoiMoveCursor());
				if (regionType==RegionType.XAXIS|| regionType==RegionType.XAXIS_LINE) {
					line1.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEWE));
				} else {
					line1.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZENS));
				}
	        	line1.setMotile(true);
	        }
		}
	}
	
	private int getLineAreaWidth() {
		if (!isTrackMouse()) {
			return WIDTH;
		}
		return 1;
	}
	
	@Override
	public void addMouseListener(MouseListener l) {
		if (line1!=null) line1.getParent().addMouseListener(l);
	}	
	
	@Override
	public void removeMouseListener(MouseListener l){
		if (line1!=null) line1.getParent().removeMouseListener(l);
	}

	
	@Override
	public void addMouseMotionListener(MouseMotionListener l){
		if (line1!=null) line1.getParent().addMouseMotionListener(l);
	}
	@Override
	public void removeMouseMotionListener(MouseMotionListener l){
		if (line1!=null) line1.getParent().removeMouseMotionListener(l);
	}
}
