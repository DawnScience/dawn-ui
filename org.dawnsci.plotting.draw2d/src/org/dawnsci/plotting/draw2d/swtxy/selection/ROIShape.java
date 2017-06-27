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
import java.util.List;

import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationEvent;
import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationListener;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.HandleStatus;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ROIHandler;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegionContainer;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Class for a shape based on a ROI and uses a ROIHandler
 */
abstract public class ROIShape<T extends IROI> extends RegionFillFigure<T> implements IRegionContainer {
	protected Figure parent;
	protected ICoordinateSystem cs;
	protected List<IFigure> handles;
	protected List<FigureTranslator> fTranslators;
	protected ROIHandler<T> roiHandler;
	protected TranslationListener handleListener;
	protected FigureListener moveListener;
	protected Rectangle bnds;
	protected boolean dirty = true;
	protected T croi;
	protected T troi = null; // temporary ROI used in dragging

	protected static final int SIDE = 8;
	protected static final double HALF_SIDE = SIDE/2.0;

	public ROIShape() {
		super(null);
		handles = new ArrayList<IFigure>();
		fTranslators = null;
	}

	public ROIShape(final Figure parent, AbstractSelectionRegion<T> region) {
		super(region);
		this.parent = parent;
		cs = region.getCoordinateSystem();
		handles = new ArrayList<IFigure>();
		fTranslators = new ArrayList<FigureTranslator>();
		roiHandler = createROIHandler(croi);
		handleListener = createHandleNotifier();
		moveListener = new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
				parent.repaint();
			}
		};
	}

	public void setCoordinateSystem(ICoordinateSystem system) {
		cs = system;
	}

	abstract protected ROIHandler<T> createROIHandler(T roi);

	abstract public void setCentre(Point nc);

	abstract public void setup(PointList corners);

	@Override
	public boolean containsPoint(int x, int y) {
		if (croi == null)
			return super.containsPoint(x, y);
		double[] pt = cs.getValueFromPosition(x, y);
		return croi.containsPoint(pt[0], pt[1]);
	}

	protected void calcBox(T proi, boolean redraw) {
		RectangularROI rroi = (RectangularROI) proi.getBounds();
		if (rroi == null) { // unbounded shape
			if (parent != null) {
				bnds = parent.getBounds();
			}
		} else {
			double[] rpt = rroi.getPointRef();
			double[] ept = rroi.getEndPoint();
			double[] bp = cs.getPositionFromValue(rpt);
			double[] ep = cs.getPositionFromValue(ept);
			bnds = new Rectangle(new PrecisionPoint(bp[0], bp[1]), new PrecisionPoint(ep[0], ep[1]));
			bnds.expand(1, 1);
		}
		if (redraw && bnds != null) {
			setBounds(bnds);
		}
		dirty = false;
	}

	public void dispose() {
		for (IFigure f : handles) {
			((SelectionHandle) f).removeMouseListeners();
		}
		for (FigureTranslator t : fTranslators) {
			t.removeTranslationListeners();
		}
		removeFigureListener(moveListener);
	}

	public T getROI() {
		return troi != null ? troi : croi;
	}

	public void configureHandles() {
		if(isGridSnap())
			snapToGrid();
		boolean mobile = region.isMobile();
		boolean visible = isVisible() && mobile;
		// handles
		FigureTranslator mover;
		final int imax = roiHandler.size();
		for (int i = 0; i < imax; i++) {
			if (roiHandler.getROI() == null) return;
			double[] hpt = roiHandler.getAnchorPoint(i, SIDE);
			double[] p = cs.getPositionFromValue(hpt);
			RectangularHandle h = addHandle(p[0], p[1], mobile, visible, handleListener);
			roiHandler.set(i, handles.indexOf(h));
		}

		addFigureListener(moveListener);
		mover = new FigureTranslator(region.getXyGraph(), parent, this, handles);
		mover.setActive(mobile);
		mover.addTranslationListener(createRegionNotifier());
		fTranslators.add(mover);
		region.setRegionObjects(this, handles);
		Rectangle b = getBounds();
		if (b != null)
			setBounds(b);
	}

	@Override
	protected FigureTranslator getFigureMover() {
		int n = fTranslators.size();
		return n > 0 ? fTranslators.get(n - 1) : null;
	}

	protected RectangularHandle addHandle(double x, double y, boolean mobile, boolean visible, TranslationListener listener) {
		RectangularHandle h = new RectangularHandle(cs, region.getRegionColor(), this, SIDE, x, y);
		h.setVisible(visible);
		parent.add(h);
		FigureTranslator mover = new FigureTranslator(region.getXyGraph(), h);
		mover.setActive(mobile);
		mover.addTranslationListener(listener);
		fTranslators.add(mover);
		h.addFigureListener(moveListener);
		handles.add(h);
		return h;
	}

	protected void removeHandle(SelectionHandle h) {
		parent.remove(h);
		h.removeMouseListeners();
		h.removeFigureListener(moveListener);
	}

	private TranslationListener createRegionNotifier() {
		return new TranslationListener() {
			@Override
			public void translateBefore(TranslationEvent evt) {
			}

			@Override
			public void translationAfter(TranslationEvent evt) {
				region.updateBounds();
				region.fireROIDragged(region.createROI(false), ROIEvent.DRAG_TYPE.TRANSLATE);
			}

			@Override
			public void translationCompleted(TranslationEvent evt) {
				region.fireROIChanged(region.createROI(true));
				roiHandler.setROI(croi);
				region.fireROISelection();
			}

			@Override
			public void onActivate(TranslationEvent evt) {
			}
		};
	}

	protected TranslationListener createHandleNotifier() {
		return new TranslationListener() {
			private double[] spt;

			@Override
			public void onActivate(TranslationEvent evt) {
				troi = null;
				Object src = evt.getSource();
				if (src instanceof FigureTranslator) {
					final FigureTranslator translator = (FigureTranslator) src;
					Point start = translator.getStartLocation();
					spt = cs.getValueFromPosition(start.x(), start.y());
					if (isGridSnap()) {
						spt[0] = Math.round(spt[0]);
						spt[1] = Math.round(spt[1]);
					}
					final IFigure handle = translator.getRedrawFigure();
					final int h = handles.indexOf(handle);
					HandleStatus status = h == roiHandler.getCentreHandle() ? HandleStatus.RMOVE : 
						(evt.controlKeyPressed() ? HandleStatus.REORIENT : HandleStatus.RESIZE);
					roiHandler.configureDragging(h, status);
				}
			}

			@Override
			public void translateBefore(TranslationEvent evt) {
			}

			@Override
			public void translationAfter(TranslationEvent evt) {
				Object src = evt.getSource();
				if (src instanceof FigureTranslator) {
					final FigureTranslator translator = (FigureTranslator) src;
					Point end = translator.getEndLocation();
					
					if (end==null) return;
					double[] c = cs.getValueFromPosition(end.x(), end.y());
					// snap to grid
					if (isGridSnap()) {
						c[0] = Math.round(c[0]);
						c[1] = Math.round(c[1]);
					}

					troi = roiHandler.interpretMouseDragging(spt, c);
					if (troi != null) {
						roiHandler.setROI(troi);
						intUpdateFromROI(troi);
						roiHandler.setROI(croi);
						
						region.fireROIDragged(troi, roiHandler.getStatus() == HandleStatus.RESIZE ?
								ROIEvent.DRAG_TYPE.RESIZE : ROIEvent.DRAG_TYPE.TRANSLATE);
					}
				}
			}

			@Override
			public void translationCompleted(TranslationEvent evt) {
				Object src = evt.getSource();
				if (src instanceof FigureTranslator) {
					troi = null;
					final FigureTranslator translator = (FigureTranslator) src;
					Point end = translator.getEndLocation();

					double[] c = cs.getValueFromPosition(end.x(), end.y());
					if (isGridSnap()) {
						c[0] = Math.round(c[0]);
						c[1] = Math.round(c[1]);
					}

					T croi = roiHandler.interpretMouseDragging(spt, c);
					if (croi != null) {
						updateFromROI(croi);
						roiHandler.unconfigureDragging();
						region.createROI(true);
	
						region.fireROIChanged(croi);
						region.fireROISelection();
					} else {
						translator.getRedrawFigure().setLocation(translator.getStartLocation());
					}
				}
			}
		};
	}

	/**
	 * Snaps selection to pixel grid. To be overridden to support different type of selections.
	 */
	abstract public void snapToGrid();

	/**
	 * Called by updateBounds in region notifiers
	 * @return
	 */
	protected Rectangle updateFromHandles() {
		if (handles.size() > 0) {
			IFigure f = handles.get(roiHandler.getCentreHandle()); // centre point
			if (f instanceof SelectionHandle) {
				SelectionHandle h = (SelectionHandle) f;
				setCentre(h.getSelectionPoint());
			}
		}
		return getBounds();
	}

	/**
	 * @return list of handle points (can be null)
	 */
	public PointList getPoints() {
		int imax = handles.size() - 1;
		if (imax < 0)
			return null;

		PointList pts = new PointList(imax);
		for (int i = 0; i < imax; i++) {
			IFigure f = handles.get(i);
			if (f instanceof SelectionHandle) {
				SelectionHandle h = (SelectionHandle) f;
				pts.addPoint(h.getSelectionPoint());
			}
		}
		return pts;
	}

	@Override
	public Rectangle getBounds() {
		T lroi = getROI();
		if (lroi != null && dirty)
			calcBox(lroi, false);
		dirty = false;
		Rectangle b = bnds == null ? super.getBounds() : new Rectangle(bnds);
		if (handles != null) {
			for (IFigure f : handles) {
				if (f instanceof SelectionHandle) {
					SelectionHandle h = (SelectionHandle) f;
					b.union(h.getBounds());
				}
			}
		}
		return b;
	}

	/**
	 * Update according to ROI
	 * @param rroi
	 */
	public void updateFromROI(T rroi) {
		croi = rroi;
		roiHandler.setROI(rroi);
		intUpdateFromROI(rroi);
	}

	/**
	 * Update according to ROI
	 * @param rroi
	 */
	private void intUpdateFromROI(T rroi) {
		int imax = handles.size();
		if (imax != roiHandler.size()) {
			for (int i = imax-1; i >= 0; i--) {
				removeHandle((SelectionHandle) handles.remove(i));
			}
			configureHandles();
		} else {
			for (int i = 0; i < imax; i++) {
				double[] hpt = roiHandler.getAnchorPoint(i, SIDE);
				SelectionHandle handle = (SelectionHandle) handles.get(i);
				double[] pta = cs.getPositionFromValue(hpt);
				handle.setSelectionPoint(new PrecisionPoint(pta[0], pta[1]));
			}
		}
		dirty = true;
		calcBox(rroi, true);
	}

	protected void drawLabel(Graphics graphics, double[] roiMidPoint) {
		if (region.isShowLabel()) {
			graphics.pushState();
			
			double[] pta = cs.getPositionFromValue(roiMidPoint);
			graphics.setForegroundColor(ColorConstants.black);
			graphics.setAlpha(255);
			graphics.drawText(region.getName(), new PrecisionPoint(pta[0], pta[1]));
			
			graphics.popState();
		}
	}

	/**
	 * 
	 * @return true if the snap to grid option for selections/regions is toggled on
	 */
	public boolean isGridSnap() {
		return ((XYRegionGraph)region.getBean().getXyGraph()).isGridSnap();
	}
}
