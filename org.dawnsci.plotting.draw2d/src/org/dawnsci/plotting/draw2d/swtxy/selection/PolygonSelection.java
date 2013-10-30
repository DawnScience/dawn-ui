/*-
 * Copyright 2012 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegionContainer;
import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.dawnsci.plotting.draw2d.swtxy.util.RotatablePolygonShape;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;

public class PolygonSelection extends AbstractSelectionRegion {

	DecoratedPolygon pline;
	private static final Color magenta = new Color(null, 238, 0,	238);

	public PolygonSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(magenta);
		setAlpha(80);
		setLineWidth(2);
	}

	@Override
	public void setVisible(boolean visible) {
		getBean().setVisible(visible);
		if (pline != null)
			pline.setVisible(visible);
	}

	@Override
	public void setMobile(final boolean mobile) {
		getBean().setMobile(mobile);
		if (pline != null)
			pline.setMobile(mobile);
	}

	@Override
	public void createContents(Figure parent) {
		pline = new DecoratedPolygon(parent);
		pline.setCursor(Draw2DUtils.getRoiMoveCursor());

		parent.add(pline);
		sync(getBean());
		setOpaque(false);
		setAlpha(getAlpha());
		pline.setForegroundColor(getRegionColor());
		pline.setAlpha(getAlpha());
		updateROI();
		if (roi == null)
			createROI(true);
	}

	@Override
	public boolean containsPoint(double x, double y) {
		final int[] pix = coords.getValuePosition(x,y);
		return pline.containsPoint(pix[0], pix[1]);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.POLYGON;
	}

	@Override
	protected void updateConnectionBounds() {
		if (pline != null) {
			Rectangle b = pline.updateFromHandles();
			pline.setBounds(b);
		}
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		g.setLineStyle(SWT.LINE_DOT);
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.drawPolygon(clicks);
	}

	@Override
	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
		if (pline != null) {
			pline.setPoints(clicks);
			setRegionColor(getRegionColor());
			setOpaque(false);
			setAlpha(getAlpha());
			updateConnectionBounds();
			createROI(true);
			fireROIChanged(getROI());
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-polygon.png";
	}

	@Override
	protected IROI createROI(boolean recordResult) {
		final PointList pl = pline.getPoints();
		final PolygonalROI proi = new PolygonalROI();
		for (int i = 0, imax = pl.size(); i < imax; i++) {
			Point p = pl.getPoint(i);
			proi.insertPoint(i, coords.getPositionValue(p.x(),p.y()));
		}
		if (roi != null) {
			proi.setPlot(roi.isPlot());
			// set the Region isActive flag
			this.setActive(roi.isPlot());
		}
		if (recordResult)
			roi = proi;

		return proi;
	}

	@Override
	protected void updateROI(IROI roi) {
		if (roi instanceof PolylineROI) {
			if (pline == null)
				return;

			pline.updateFromROI((PolylineROI) roi);

			updateConnectionBounds();
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 0; // signifies unlimited presses
	}

	@Override
	public void dispose() {
		super.dispose();
		if (pline != null) {
			pline.dispose();
		}
	}

	class DecoratedPolygon extends RotatablePolygonShape implements IRegionContainer {
		List<IFigure> handles;
		List<FigureTranslator> fTranslators;
		private Figure parent;
		private FigureListener moveListener;
		private static final int SIDE = 8;

		public DecoratedPolygon(Figure parent) {
			super();
			handles = new ArrayList<IFigure>();
			fTranslators = new ArrayList<FigureTranslator>();
			this.parent = parent;
			moveListener = new FigureListener() {
				@Override
				public void figureMoved(IFigure source) {
					DecoratedPolygon.this.parent.repaint();
				}
			};
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

		@Override
		public void setPoints(PointList points) {
			super.setPoints(points);

			final Point p = new Point();
			boolean mobile = isMobile();
			boolean visible = isVisible() && mobile;
			for (int i = 0, imax = points.size(); i < imax; i++) {
				points.getPoint(p, i);
				addHandle(p.preciseX(), p.preciseY(), mobile, visible);
			}

			addFigureListener(moveListener);
			FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, this, handles);
			mover.setActive(isMobile());
			mover.addTranslationListener(createRegionNotifier());
			fTranslators.add(mover);

			setRegionObjects(this, handles);
		}

		private Rectangle updateFromHandles() {
			int i = 0;
			Rectangle b = null;
			for (IFigure f : handles) { // this is called first so update points
				if (f instanceof SelectionHandle) {
					SelectionHandle h = (SelectionHandle) f;
					setPoint(h.getSelectionPoint(), i++);
					if (b == null) {
						b = new Rectangle(h.getBounds());
					} else {
						b.union(h.getBounds());
					}
				}
			}
			return b;
		}

		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			boolean hVisible = visible && isMobile();
			for (IFigure h : handles) {
				h.setVisible(hVisible);
			}
		}

		public void setMobile(boolean mobile) {
			for (IFigure h : handles) {
				h.setVisible(mobile);
			}
			for (FigureTranslator f : fTranslators) {
				f.setActive(mobile);
			}
		}

		private void removeHandle(SelectionHandle h) {
			parent.remove(h);
			h.removeMouseListeners();
		}

		private void addHandle(double x, double y, boolean mobile, boolean visible) {
			RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE, x, y);
			h.setVisible(visible);
			parent.add(h);
			FigureTranslator mover = new FigureTranslator(getXyGraph(), h);
			mover.setActive(mobile);
			mover.addTranslationListener(createRegionNotifier());
			fTranslators.add(mover);
			h.addFigureListener(moveListener);
			handles.add(h);
		}

		/**
		 * Update according to ROI
		 * @param proi
		 */
		public void updateFromROI(PolylineROI proi) {
			final PointList pl = getPoints();
			int imax = handles.size();
			if (imax != proi.getNumberOfPoints()) {
				for (int i = imax-1; i >= 0; i--) {
					removeHandle((SelectionHandle) handles.remove(i));
				}
				imax = proi.getNumberOfPoints();
				int np = pl.size();
				boolean mobile = isMobile();
				boolean visible = isVisible() && mobile;
				for (int i = 0; i < imax; i++) {
					PointROI r = proi.getPoint(i);
					int[] pnt  = coords.getValuePosition(r.getPointRef());
					Point p = new Point(pnt[0], pnt[1]);
					if (i < np) {
						setPoint(p, i);
					} else {
						addPoint(p);
					}
					addHandle(pnt[0], pnt[1], mobile, visible);
				}

				addFigureListener(moveListener);
				FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, this, handles);
				mover.addTranslationListener(createRegionNotifier());
				mover.setActive(isMobile());
				fTranslators.add(mover);
				setRegionObjects(this, handles);
				return;
			}

			for (int i = 0; i < imax; i++) {
				PointROI p = proi.getPoint(i);
				int[] pnt  = coords.getValuePosition(p.getPointRef());
				Point np = new Point(pnt[0], pnt[1]);
				pl.setPoint(np, i);
				SelectionHandle h = (SelectionHandle) handles.get(i);
				h.setSelectionPoint(np);
			}
		}

		@Override
		public IRegion getRegion() {
			return PolygonSelection.this;
		}

		@Override
		public void setRegion(IRegion region) {
		}
	}
}
