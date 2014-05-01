/*-
 * Copyright 2014 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.diamond.scisoft.analysis.roi.IPolylineROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.diamond.scisoft.analysis.roi.handler.ROIHandler;

/**
 * Class for a shape based on a polyline ROI and does not use a ROIHandler
 */
abstract public class PolylineROIShape<T extends IPolylineROI> extends ROIShape<T> {
	protected PointList points;

	public PolylineROIShape(Figure parent, AbstractSelectionRegion region) {
		super(parent, region);
	}

	@Override
	protected ROIHandler<T> createROIHandler(IPolylineROI roi) {
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
			croi.insertPoint(new PointROI(cs.getPositionValue(p.x(), p.y())));
		}

		region.createROI(true);
		configureHandles();
	}

	@Override
	protected void configureHandles() {
		Rectangle b = null;
		boolean mobile = region.isMobile();
		boolean visible = isVisible() && mobile;
		final Point p = new Point();
		for (int i = 0, imax = points.size(); i < imax; i++) {
			points.getPoint(p, i);
			Rectangle bh = addHandle(p.x, p.y(), mobile, visible);
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
				croi.setPoint(i++, new PointROI(cs.getPositionValue(pt.x(), pt.y())));
				if (b == null) {
					b = new Rectangle(h.getBounds());
				} else {
					b.union(h.getBounds());
				}
			}
		}
		return b;
	}

	public void setMobile(boolean mobile) {
		for (FigureTranslator f : fTranslators) {
			f.setActive(mobile);
		}
	}

	private void removeHandle(SelectionHandle h) {
		parent.remove(h);
		h.removeMouseListeners();
	}

	private Rectangle addHandle(int x, int y, boolean mobile, boolean visible) {
		RectangularHandle h = new RectangularHandle(cs, region.getRegionColor(), this, SIDE, x, y);
		h.setVisible(visible);
		parent.add(h);
		FigureTranslator mover = new FigureTranslator(region.getXyGraph(), h);
		mover.setActive(mobile);
		mover.addTranslationListener(region.createRegionNotifier());
		fTranslators.add(mover);
		h.addFigureListener(moveListener);
		handles.add(h);
		return h.getBounds();
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
				int[] pnt  = cs.getValuePosition(r.getPointRef());
				points.addPoint(pnt[0], pnt[1]);
				Rectangle hb = addHandle(pnt[0], pnt[1], mobile, visible);
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
			for (int i = 0; i < imax; i++) {
				IROI p = proi.getPoint(i);
				if (proi != croi)
					croi.setPoint(i, p);
				int[] pnt = cs.getValuePosition(p.getPointRef());
				points.setPoint(new Point(pnt[0], pnt[1]), i);
				SelectionHandle h = (SelectionHandle) handles.get(i);
				h.setSelectionPoint(new Point(pnt[0], pnt[1]));
				Rectangle hb = h.getBounds();
				if (b == null) {
					b = new Rectangle(hb);
				} else {
					b.union(hb);
				}
			}
		}

		if (b != null)
			setBounds(b);
	}
}
