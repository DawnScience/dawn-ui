/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationListener;
import org.eclipse.dawnsci.analysis.api.roi.IPolylineROI;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ROIHandler;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Class for a shape based on a polyline ROI and does not use a ROIHandler
 */
abstract public class PolylineROIShape<T extends IPolylineROI> extends ROIShape<T> {
	protected PointList points;

	public PolylineROIShape(Figure parent, AbstractSelectionRegion<T> region) {
		super(parent, region);
	}

	@Override
	protected ROIHandler<T> createROIHandler(T roi) {
		return null;
	}

	@Override
	public void setCentre(Point nc) {
	}

	abstract protected T createNewROI();

	@Override
	public void setup(PointList points) {
		croi = createNewROI();
		this.points = points;

		final Point p = new Point();
		for (int i = 0, imax = points.size(); i < imax; i++) {
			points.getPoint(p, i);
			croi.insertPoint(new PointROI(cs.getValueFromPosition(p.x(), p.y())));
		}

		region.createROI(true);
		configureHandles();
	}

	@Override
	protected TranslationListener createHandleNotifier() {
		return region.createRegionNotifier();
	}

	@Override
	public void configureHandles() {
		Rectangle b = null;
		boolean mobile = region.isMobile();
		boolean visible = isVisible() && mobile;
		final Point p = new Point();
		for (int i = 0, imax = points.size(); i < imax; i++) {
			points.getPoint(p, i);
			Rectangle bh = addHandle(p.x, p.y(), mobile, visible, handleListener).getBounds();
			if (b == null) {
				b = new Rectangle(bh);
			} else {
				b.union(bh);
			}
		}

		addFigureListener(moveListener);
		FigureTranslator mover = new FigureTranslator(region.getXyGraph(), parent, this, handles);
		mover.setActive(mobile);
		mover.addTranslationListener(region.createRegionNotifier());
		fTranslators.add(mover);

		region.setRegionObjects(this, handles);
		if (b != null)
			setBounds(b);
	}

	@Override
	protected Rectangle updateFromHandles() {
		int i = 0;
		Rectangle b = null;
		for (IFigure f : handles) { // this is called first so update points
			if (f instanceof SelectionHandle) {
				SelectionHandle h = (SelectionHandle) f;
				Point pt = h.getSelectionPoint();
				points.setPoint(pt, i);
				croi.setPoint(i++, new PointROI(cs.getValueFromPosition(pt.x(), pt.y())));
				if (b == null) {
					b = new Rectangle(h.getBounds());
				} else {
					b.union(h.getBounds());
				}
			}
		}
		return b;
	}

	/**
	 * Update according to ROI
	 * @param proi
	 */
	public void updateFromROI(T proi) {
		int imax = handles.size();
		if (points == null) {
			points = new PointList(proi.getNumberOfPoints());
		}
		if (croi == null) {
			croi = proi;
		}

		Rectangle b = null;
		if (imax != proi.getNumberOfPoints()) {
			if (proi != croi)
				croi.removeAllPoints();
			points.removeAllPoints();
			for (int i = imax-1; i >= 0; i--) {
				removeHandle((SelectionHandle) handles.remove(i));
			}
			boolean mobile = region.isMobile();
			boolean visible = isVisible() && mobile;
			for (IROI r: proi) {
				if (proi != croi)
					croi.insertPoint(r);
				double[] pnt  = cs.getPositionFromValue(r.getPointRef());
				points.addPoint((int) pnt[0], (int) pnt[1]);
				Rectangle hb = addHandle(pnt[0], pnt[1], mobile, visible, handleListener).getBounds();
				if (b == null) {
					b = new Rectangle(hb);
				} else {
					b.union(hb);
				}
			}
			addFigureListener(moveListener);
			FigureTranslator mover = new FigureTranslator(region.getXyGraph(), parent, this, handles);
			mover.addTranslationListener(region.createRegionNotifier());
			mover.setActive(region.isMobile());
			fTranslators.add(mover);
			region.setRegionObjects(this, handles);
		} else {
			if (troi != null) { // do not update if there's dragging going on 
				for (int i = 0; i < imax; i++) {
					IROI p = proi.getPoint(i);
					if (proi != croi)
						croi.setPoint(i, p);
					double[] pnt = cs.getPositionFromValue(p.getPointRef());
					Point pt = new PrecisionPoint(pnt[0], pnt[1]);
					points.setPoint(pt, i);
					SelectionHandle h = (SelectionHandle) handles.get(i);
					h.setSelectionPoint(pt);
					Rectangle hb = h.getBounds();
					if (b == null) {
						b = new Rectangle(hb);
					} else {
						b.union(hb);
					}
				}
			}
		}

		if (b != null)
			setBounds(b);
	}
}
