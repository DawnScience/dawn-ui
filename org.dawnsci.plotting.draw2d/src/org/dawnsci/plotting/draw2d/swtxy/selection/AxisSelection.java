/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.plotting.draw2d.swtxy.IMobileFigure;
import org.dawnsci.plotting.draw2d.swtxy.RegionArea;
import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisLineBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisLineBoxROI;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.region.ILockTranslatable;
import org.eclipse.dawnsci.plotting.api.region.IRegionContainer;
import org.eclipse.dawnsci.plotting.api.region.MouseListener;
import org.eclipse.dawnsci.plotting.api.region.MouseMotionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ITraceContainer;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionDimension;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *     Either
 *     |   |
 *     |   |
 *     
 *     or
 *     ------------
 *     ------------
 *     
 * @author Matthew Gerring
 */
class AxisSelection extends AbstractSelectionRegion<RectangularROI> implements ILockTranslatable {
	
	private static final Logger logger = LoggerFactory.getLogger(AxisSelection.class);
		
	private static final int WIDTH = 8;
	
	private LineFigure line1, line2;
	private Figure     connection;
	private RegionArea regionArea;
	private RegionType regionType;

	private org.eclipse.draw2d.MouseMotionListener mouseTrackListener;

	private FigureTranslator mover;

	private boolean isMoving = false;
	
	AxisSelection(String name, ICoordinateSystem coords, RegionType regionType) {
		super(name, coords);
		if (regionType!=RegionType.XAXIS && regionType!=RegionType.YAXIS && regionType!=RegionType.XAXIS_LINE && regionType!=RegionType.YAXIS_LINE) {
			throw new RuntimeException("The AxisSelection can only be XAXIS or YAXIS region type!");
		}
		setRegionColor(ColorConstants.darkGreen);	
		setAlpha(80);
		setLineWidth(1);
		this.regionType = regionType;
	}

	@Override
	public void createContents(final Figure parent) {
		
		this.regionArea = (RegionArea)parent;
     	this.line1 = new LineFigure(parent.getBounds());
     	
     	
     	if (regionType==RegionType.XAXIS || regionType==RegionType.YAXIS) {
    	    this.line2 = new LineFigure(parent.getBounds());
    	    this.connection = new RegionFillFigure<RectangularROI>(this) {
    	    	@Override
    	    	public void paintFigure(Graphics gc) {
    	    		super.paintFigure(gc);
    	    		final Rectangle size = getRectangleFromVertices();				
    	    		this.bounds = size;
    	    		gc.setAlpha(getAlpha());
    	    		gc.fillRectangle(size);

    	    		AxisSelection.this.drawLabel(gc, size);
    	    	}

				@Override
				protected void fillShape(Graphics graphics) {
				}

				@Override
				protected void outlineShape(Graphics graphics) {
				}
    	    };
       	    connection.setOpaque(false);
    	    connection.setEnabled(true);
    	    connection.setCursor(Draw2DUtils.getRoiMoveCursor());
    	    connection.setBackgroundColor(getRegionColor());
    	    connection.setBounds(new Rectangle(line1.getLocation(), line2.getBounds().getBottomRight()));
 
    		parent.add(connection);
    		parent.add(line2);
        }
     	
		parent.add(line1);
				

     	if (regionType==RegionType.XAXIS || regionType==RegionType.YAXIS) {
    		this.mover = new FigureTranslator(getXyGraph(), parent, connection, Arrays.asList(new IFigure[]{line1, line2}));
    		
    		if (isDrawnOnX()) {
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
        
        parent.addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
				if (line1!=null) line1.updateBounds(source.getBounds(),false);
				if (line2!=null) line2.updateBounds(source.getBounds(),false);
				updateBounds();
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
    			IFigure into = parent.findFigureAt(me.getLocation());
    			
    			// If the region found was not to our liking at this location we look
    			// around the vicinity. This is needed because sometimes the location
    			// crosses the region we are on in cross-hair mode.
    			REGION_TEST: if (into instanceof AxisSelection || into instanceof LineFigure){
    			    into = parent.findFigureAt(me.getLocation().x+2, me.getLocation().y+2);
    			    if (into instanceof AbstractRegion || into instanceof IRegionContainer &&
    			        !(into instanceof AxisSelection) && !(into instanceof LineFigure)) break REGION_TEST;
    			    
    			    into = parent.findFigureAt(me.getLocation().x-2, me.getLocation().y-2);
    			    if (into instanceof AbstractRegion || into instanceof IRegionContainer &&
    			        !(into instanceof AxisSelection) && !(into instanceof LineFigure)) break REGION_TEST;
    			}
    			
    			if (into==null) {
    				setVisible(false);
    			} else {
    				extraFigures.add(into);
    				into.addMouseMotionListener(this);
        			fireROIChanged();
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

	@Override
	public void paintBeforeAdded(final Graphics gc, PointList clicks, Rectangle parentBounds) {
		
		final Rectangle bounds = new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint());
		Rectangle r = getSelectionBounds(bounds.resize(-1, -1), parentBounds); // as constructor grows rectangle

		gc.setLineStyle(Graphics.LINE_DOT);
		
		boolean isBox = regionType==RegionType.XAXIS || regionType==RegionType.YAXIS;
		
		if (isDrawnOnX()) {
			gc.drawLine(r.getBottomLeft(), r.getTopLeft());
			if (isBox) gc.drawLine(r.getBottomRight(), r.getTopRight());
		} else {
			gc.drawLine(r.getTopLeft(), r.getTopRight());
			if (isBox) gc.drawLine(r.getBottomLeft(), r.getBottomRight());
		}
		gc.setBackgroundColor(getRegionColor());
		gc.setAlpha(getAlpha());
		gc.fillRectangle(r);
	}

	
	private Rectangle getSelectionBounds(Rectangle bounds,
			                             Rectangle parentBounds) {
		
		Rectangle r;
		if (isDrawnOnX()) { // extend to top and bottom of parent
			r = new Rectangle(new Point(bounds.x, parentBounds.y),
					new Dimension(bounds.width, parentBounds.height));
		} else { // extend to left and right of parent
			r = new Rectangle(new Point(parentBounds.x, bounds.y),
					new Dimension(parentBounds.width, bounds.height));
		}
		
		return r;
	}

	@Override
	protected String getCursorPath() {
		if (regionType==RegionType.XAXIS || regionType==RegionType.XAXIS_LINE) {
			return "icons/Cursor-horiz.png";
		} else {
			return "icons/Cursor-vert.png";
		}
	}
	
	protected final class LineFigure extends Figure implements IMobileFigure {
		private FigureTranslator mover;
		
		LineFigure(Rectangle parent) {
			setOpaque(false);
			updateBounds(parent,true);			
			this.mover = new FigureTranslator(getXyGraph(), this);
			
			if (isDrawnOnX()) {
				mover.setLockedDirection(FigureTranslator.LockType.X);
			} else {
				mover.setLockedDirection(FigureTranslator.LockType.Y);
			}
			mover.addTranslationListener(createRegionNotifier());
			addFigureListener(createFigureListener());
			mover.setActive(isMobile());
		}
		
		private void updateBounds(Rectangle parentBounds, boolean init) {

			if (isDrawnOnX()) {
				if (!isTrackMouse()) setCursor(Cursors.SIZEWE);

				int x = parentBounds.x;
				int w = getLineAreaWidth();

				Rectangle b = this.getBounds();

				if (b != null && !init) {
					x = b.x;
					w = b.width;
				}

				setBounds(new Rectangle(x, parentBounds.y, w, parentBounds.height));
			} else {
				if (!isTrackMouse()) setCursor(Cursors.SIZENS);

				int y = parentBounds.y;
				int h = getLineAreaWidth();

				Rectangle b = this.getBounds();

				if (b != null && !init) {
					y = b.y;
					h = b.height;
				}

				setBounds(new Rectangle(parentBounds.x, y, parentBounds.width, h));
			}
		}
		
		public void updateLockedDirection() {
			
			if (isDrawnOnX()) {
				mover.setLockedDirection(FigureTranslator.LockType.X);
			} else {
				mover.setLockedDirection(FigureTranslator.LockType.Y);
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

			if (isDrawnOnX()) {
				gc.drawLine(b.getTopLeft(), b.getBottomLeft());
			} else {
				gc.drawLine(b.getTopLeft(), b.getTopRight());
			}
		}

		public void setMobile(boolean mobile) {
			mover.setActive(mobile);
			if (!mobile) {
				setCursor(null);
			} else {
				if (isDrawnOnX()) {
					if (!isTrackMouse()) setCursor(Cursors.SIZEWE);
				} else {
					if (!isTrackMouse()) setCursor(Cursors.SIZENS);
				}			
			}
		}
	}

	@Override
	public void setMobile(boolean mobile) {
		if (connection!=null) {
			connection.setCursor(mobile?Draw2DUtils.getRoiMoveCursor():null);
			connection.setOpaque(mobile);
			connection.setEnabled(mobile);
			regionArea.setRequirePositionWithCursor(!mobile);
			if (mobile) {
				connection.addMouseListener(mover);
			} else {
				connection.removeMouseListener(mover);
			}
		}
		if (regionType==RegionType.XAXIS || regionType==RegionType.YAXIS) {
			super.setMobile(mobile);
			return;
		}
		
		if (line1!=null) line1.setMobile(mobile);
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

 		Rectangle otherBounds = line2 == null ? line1.getBounds() : line2.getBounds();
		final Point loc4 = isDrawnOnX() ? otherBounds.getBottomLeft() : otherBounds.getTopRight();
		Rectangle size = new Rectangle(loc1, loc4).resize(-1, -1); // as constructor grows rectangle

		return size;
	}

	private void reverse(double[] a1, double[] a2, int i) {
		double tmp = a1[i];
		a1[i] = a2[i];
		a2[i] = tmp;
	}

	private RectangularROI getRoiFromRectangle(final Rectangle rect) {

		double[] a1 = coords.getValueFromPosition(rect.x, rect.y);
		double[] a2 = coords.getValueFromPosition(rect.x+rect.width, rect.y+rect.height);
		if (coords.isXReversed()) reverse(a1,a2,0);
		if (coords.isYReversed()) reverse(a1,a2,1);

		double x = a1[0]; double y = a1[1];
		double w = a2[0] - a1[0]; double h = a2[1] - a1[1];

		if (w<0) {
			w = Math.abs(w);
			x-= w;
		}
		if (h<0) {
			h = Math.abs(h);
			y-= h;
		}

		return createROI(x, y, w, h, 0);
	}
	
	@Override
	public RectangularROI createROI(boolean recordResult) {
		if (line1!=null) {
			isMoving = !recordResult;
			final Rectangle rect = getRectangleFromVertices();
			final RectangularROI rroi = getRoiFromRectangle(rect);
			rroi.setName(getName());
			if (recordResult)
				roi = rroi;
			return rroi;
		}
		return super.getROI();
	}

	private RectangularROI createROI(double ptx, double pty, double width, double height, double angle) {
		RectangularROI rroi = null;
		switch (regionType) {
		case XAXIS:
			rroi = new XAxisBoxROI(ptx, pty, width, height, angle);
			break;
		case YAXIS:
			rroi = new YAxisBoxROI(ptx, pty, width, height, angle);
			break;
		case XAXIS_LINE:
			rroi = new XAxisLineBoxROI(ptx, pty, width, height, angle);
			break;
		case YAXIS_LINE:
			rroi = new YAxisLineBoxROI(ptx, pty, width, height, angle);
			break;
		default:
			rroi = new RectangularROI(ptx, pty, width, height, angle);
		}
		return rroi;
	}
	
	private boolean isDrawnOnX() {
		boolean isX = (regionType==RegionType.XAXIS || regionType==RegionType.XAXIS_LINE);
		
		if (coords != null && !coords.isCoordsFlipped()) {
			isX = !isX;
		}
		
		return isX;
	}

	protected void updateRegion() {
		
		if (line1 == null || roi == null || isMoving) return;

		if (!(roi instanceof RectangularROI)) {
			roi = convertROI(roi);
			if (!(roi instanceof RectangularROI)) {
				return;
			}
		}

		RectangularROI rroi = (RectangularROI) roi;
		final double[] p1 = coords.getPositionFromValue(rroi.getPointRef());
		final double[] p2 = coords.getPositionFromValue(rroi.getEndPoint());

		final PrecisionRectangle local = new PrecisionRectangle(new PrecisionPoint(p1[0], p1[1]), new PrecisionDimension(p2[0] - p1[0], p2[1] - p1[1]));

		setLocalBounds(local, line1.getParent().getBounds());
		
		if (mover != null) {
			if (isDrawnOnX()) {
				mover.setLockedDirection(FigureTranslator.LockType.X);
			} else {
				mover.setLockedDirection(FigureTranslator.LockType.Y);
			}
		}
		
		if (line1 != null) {
			line1.updateLockedDirection();
		}
		
		if (line2 != null) {
			line2.updateLockedDirection();
		}
		
		
	}

	@Override
	protected RectangularROI convertROI(IROI oroi) {
		if (oroi instanceof LinearROI) {
			LinearROI lroi = (LinearROI) oroi;
			return new RectangularROI(lroi.getPoint(), lroi.getEndPoint());
		}
		return super.convertROI(oroi);
	}

	/**
	 * Sets the local in local coordinates
	 * @param bounds
	 */
	@Override
	public void initialize(PointList clicks) {
		if (line1!=null) {
			final Rectangle bounds = new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint());
			setLocalBounds(bounds.resize(-1, -1), regionArea.getBounds()); // as constructor grows rectangle
			createROI(true);
			fireROIChanged();
		}
	}

	protected void setLocalBounds(Rectangle box, Rectangle parentBounds) {
		if (line1!=null) {
			final Rectangle bounds = getSelectionBounds(box, parentBounds);

			if (isDrawnOnX()) {
				line1.setLocation(bounds.getTopLeft());
				if (line2!=null) line2.setLocation(bounds.getTopRight());
			} else {
				line1.setLocation(bounds.getTopLeft());
				if (line2!=null) line2.setLocation(bounds.getBottomLeft());
			}
		}
		updateBounds();
		
	}

	@Override
	protected void updateBounds() {
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
	        	line1.setMobile(false); // Not moved by the user, moved by the mouse position.
	        } else {
				regionArea.setRequirePositionWithCursor(true);
	        	line1.getParent().removeMouseMotionListener(mouseTrackListener);
	        	line1.getParent().setCursor(null);
	        	line1.setEnabled(true);// This starts the figure part of mouse listeners
	        	if (connection!=null) {
	        	    connection.setEnabled(true);
	        		connection.setCursor(Draw2DUtils.getRoiMoveCursor());
	        	}
	        	
				if (isDrawnOnX()) {
					line1.setCursor(Cursors.SIZEWE);
				} else {
					line1.setCursor(Cursors.SIZENS);
				}
	        	line1.setMobile(true);
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
	public void addMouseListener(final MouseListener l) {
		if (line1!=null) {
			try {
				line1.getParent().addMouseListener(registerMouseListener(l));
			} catch(final IllegalStateException e) {
				logger.debug("Illegal state on adding mouse listener",e);
			}
		}
	}

	@Override
	public void removeMouseListener(final MouseListener l){
		if (line1!=null) {
			try {
				line1.getParent().removeMouseListener(unregisterMouseListener(l));
			} catch(final IllegalStateException e) {
				logger.debug("Illegal state on removing mouse listener",e);
			}
		}
	}

	@Override
	public void addMouseMotionListener(final MouseMotionListener l){
		if (line1!=null) {
			try {
				line1.getParent().addMouseMotionListener(registerMouseMotionListener(l));
			} catch(final IllegalStateException e) {
				logger.debug("Illegal state on adding mouse motion listener",e);
			}
		}
	}

	@Override
	public void removeMouseMotionListener(final MouseMotionListener l){
		if (line1!=null) {
			try {
				line1.getParent().removeMouseMotionListener(unregisterMouseMotionListener(l));
			} catch(final IllegalStateException e) {
				logger.debug("Illegal state on removing mouse motion listener",e);
			}
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return regionType == RegionType.XAXIS || regionType == RegionType.YAXIS ? 2 : 1;
	}

	public final void remove() {
		super.remove();
		regionArea.setRequirePositionWithCursor(true);
		regionArea = null;
	}

	@Override
	public void snapToGrid() {
		// TODO implement the snap to grid for this selection
		
	}
	
	@Override
	public void translateOnly(boolean fix) {
		
		if (line1 != null) {
			line1.setMobile(!fix);
			line1.setVisible(!fix);
		}

		if (line2 != null) {
			line2.setMobile(!fix);
			line2.setVisible(!fix);
		}
	}
}
