package org.dawnsci.plotting.draw2d.swtxy.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.api.region.MouseListener;
import org.dawnsci.plotting.api.region.MouseMotionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.trace.ITraceContainer;
import org.dawnsci.plotting.draw2d.swtxy.IMobileFigure;
import org.dawnsci.plotting.draw2d.swtxy.RegionArea;
import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
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
	private Figure     connection;
	private RegionArea regionArea;
	private RegionType regionType;

	private org.eclipse.draw2d.MouseMotionListener mouseTrackListener;

	
	AxisSelection(String name, ICoordinateSystem coords, RegionType regionType) {
		super(name, coords);
		if (regionType!=RegionType.XAXIS && regionType!=RegionType.YAXIS && regionType!=RegionType.XAXIS_LINE && regionType!=RegionType.YAXIS_LINE) {
			throw new RuntimeException("The AxisSelection can only be XAXIS or YAXIS region type!");
		}
		setRegionColor(ColorConstants.blue);	
		setAlpha(80);
		setLineWidth(1);
		this.regionType = regionType;
	}

	@Override
	public void createContents(final Figure parent) {
		
		this.regionArea  = (RegionArea)parent;
     	this.line1       = new LineFigure(true,  parent.getBounds());
     	
     	if (regionType==RegionType.XAXIS || regionType==RegionType.YAXIS) {
    	    this.line2  = new LineFigure(false, parent.getBounds());
    	    this.connection = new RegionFillFigure(this) {
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
        updateROI();
        if (roi ==null) createROI(true);

        
        parent.addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
				if (line1!=null) line1.updateBounds(source.getBounds());
				if (line2!=null) line2.updateBounds(source.getBounds());
				updateConnectionBounds();
			}
		});
        
        this.mouseTrackListener = new org.eclipse.draw2d.MouseMotionListener.Stub() {
        	
        	private List<IFigure> extraFigures = new ArrayList<IFigure>();
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
	    			
	    			fireROIDragged(createROI(true), ROIEvent.DRAG_TYPE.TRANSLATE);
    			}
    		}
    		
    		/**
    		 * @see org.eclipse.draw2d.MouseMotionListener#mouseEntered(MouseEvent)
    		 */
    		public void mouseEntered(MouseEvent me) {
    			final IFigure into = parent.findFigureAt(me.getLocation());
    			if (into instanceof ITraceContainer || into == parent) {
    				for (IFigure figure : extraFigures) figure.removeMouseMotionListener(this);
    				extraFigures.clear();
    			}
    			setVisible(true);
    		}

    		/**
    		 * @see org.eclipse.draw2d.MouseMotionListener#mouseExited(MouseEvent)
    		 */
    		public void mouseExited(MouseEvent me) {
    			
    			final IFigure into = parent.findFigureAt(me.getLocation());
    			if (into==null) {
    				setVisible(false);
    			} else if (into!=AxisSelection.this && (AxisSelection.this).line1!=into && into != parent){
    				extraFigures.add(into);
    				into.addMouseMotionListener(this);
    			}
    			fireROIChanged(getROI());
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

	@Override
	public void paintBeforeAdded(final Graphics gc, PointList clicks, Rectangle parentBounds) {
		
		final Rectangle bounds = new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint());
		Rectangle r  = getSelectionBounds(bounds, parentBounds);
		
		
		gc.setLineStyle(Graphics.LINE_DOT);
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

	@Override
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
				if (!isTrackMouse()) setCursor(Cursors.SIZEWE);
				setBounds(new Rectangle(parentBounds.x, parentBounds.y, getLineAreaWidth(), parentBounds.height));
			} else {
				if (!isTrackMouse()) setCursor(Cursors.SIZENS);
				setBounds(new Rectangle(parentBounds.x, parentBounds.y, parentBounds.width, getLineAreaWidth()));
			}
		}
		protected void paintFigure(Graphics gc) {
			
			super.paintFigure(gc);
			
			if (regionType==RegionType.XAXIS || regionType==RegionType.YAXIS) {
				gc.setLineStyle(Graphics.LINE_DOT);
			} else {
				gc.setLineStyle(Graphics.LINE_SOLID);
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
			if (!motile) {
				setCursor(null);
			} else {
				if (regionType==RegionType.XAXIS|| regionType==RegionType.XAXIS_LINE) {
					if (!isTrackMouse()) setCursor(Cursors.SIZEWE);
				} else {
					if (!isTrackMouse()) setCursor(Cursors.SIZENS);
				}			
			}
		}
	}

	@Override
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

	protected Rectangle getRectangleFromVertices() {
		final Point loc1   = line1.getLocation();
		final Point loc4   = line2 != null 
				           ? line2.getBounds().getBottomRight()
				           : line1.getBounds().getBottomRight();
		Rectangle size = new Rectangle(loc1, loc4);
		return size;
	}

	@Override
	public IROI createROI(boolean recordResult) {
		if (line1!=null) {
			final Rectangle rect = getRectangleFromVertices();
			final RectangularROI rroi = getRoiFromRectangle(rect);
			if (recordResult)
				roi = rroi;
			return rroi;
		}
		return super.getROI();
	}
	
	protected void updateROI(IROI roi) {
		
		if (line1 == null) return;
		
		double[] spt = null;
		double[] ept = null;

		if (roi instanceof RectangularROI) {
			RectangularROI rroi = (RectangularROI) roi;
			spt = rroi.getPointRef();
			ept = rroi.getEndPoint();
		} else if (roi instanceof LinearROI) {
			LinearROI lroi = (LinearROI) roi;
			spt = lroi.getPointRef();
			ept = lroi.getEndPoint();			
		}
		
		final int[] p1 = coords.getValuePosition(spt);
		final int[] p2 = coords.getValuePosition(ept);

		final Rectangle local = new Rectangle(new Point(p1[0], p1[1]), new Point(p2[0], p2[1]));
		setLocalBounds(local, line1.getParent().getBounds());
		updateConnectionBounds();

	}

	/**
	 * Sets the local in local coordinates
	 * @param bounds
	 */
	@Override
	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
		if (line1!=null) {
			setLocalBounds(new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint()), parentBounds);
			createROI(true);
			fireROIChanged(getROI());
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

	@Override
	protected void updateConnectionBounds() {
		if (connection==null) return;
		final Rectangle size = getRectangleFromVertices();				
		connection.setBounds(size);
	}
	
	@Override
	public RegionType getRegionType() {
		return regionType;
	}

	@Override
	public void setTrackMouse(boolean track) {
		super.setTrackMouse(track);
       
		if (line1!=null) {
			if (isTrackMouse()) {
				regionArea.setRequirePositionWithCursor(false);
				line1.setEnabled(false);// This stops the figure being part of mouse listeners
				line1.getParent().addMouseMotionListener(mouseTrackListener);
	        	line1.getParent().setCursor(Cursors.CROSS);
	        	line1.setCursor(null);
	        	if (connection!=null) connection.setCursor(null);
	        	line1.setMotile(false); // Not moved by the user, moved by the mouse position.
	        } else {
				regionArea.setRequirePositionWithCursor(true);
	        	line1.getParent().removeMouseMotionListener(mouseTrackListener);
	        	line1.getParent().setCursor(null);
	        	line1.setEnabled(true);// This starts the figure part of mouse listeners
	        	if (connection!=null) connection.setCursor(Draw2DUtils.getRoiMoveCursor());
				if (regionType==RegionType.XAXIS|| regionType==RegionType.XAXIS_LINE) {
					line1.setCursor(Cursors.SIZEWE);
				} else {
					line1.setCursor(Cursors.SIZENS);
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
		if (line1!=null) line1.getParent().addMouseListener(new MouseListenerAdapter(l));
	}	
	
	@Override
	public void removeMouseListener(MouseListener l){
		if (line1!=null) line1.getParent().removeMouseListener(new MouseListenerAdapter(l));
	}

	@Override
	public void addMouseMotionListener(MouseMotionListener l){
		if (line1!=null) line1.getParent().addMouseMotionListener(new MouseMotionAdapter(l));
	}

	@Override
	public void removeMouseMotionListener(MouseMotionListener l){
		if (line1!=null) line1.getParent().removeMouseMotionListener(new MouseMotionAdapter(l));
	}

	@Override
	public int getMaximumMousePresses() {
		return 2;
	}
	
	public final void remove() {
        super.remove();
        regionArea.setRequirePositionWithCursor(true);
        regionArea = null;
	}
}
