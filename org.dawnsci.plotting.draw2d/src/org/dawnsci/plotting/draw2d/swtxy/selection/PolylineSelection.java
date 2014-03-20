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
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;

public class PolylineSelection extends AbstractSelectionRegion {

	Polyline polyline;

	public PolylineSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.cyan);
		setAlpha(80);
		setLineWidth(2);
	}

	@Override
	public void setMobile(boolean mobile) {
		super.setMobile(mobile);
		if (polyline != null)
			polyline.setMobile(mobile);
	}

	@Override
	public void createContents(Figure parent) {
		polyline = new Polyline(parent, coords);
		polyline.setCursor(Draw2DUtils.getRoiMoveCursor());

		parent.add(polyline);
		sync(getBean());
		setOpaque(false);
	}

	@Override
	public boolean containsPoint(int x, int y) {
		return polyline.containsPoint(x, y);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.POLYLINE;
	}

	@Override
	protected void updateBounds() {
		if (polyline != null) {
			Rectangle b = polyline.updateFromHandles();
			polyline.setBounds(b);
		}
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		g.setLineStyle(SWT.LINE_DOT);
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.drawPolyline(clicks);
	}

	@Override
	public void initialize(PointList clicks) {
		if (polyline != null) {
			polyline.setup(clicks);
			fireROIChanged(createROI(true));
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-polyline.png";
	}

	@Override
	protected IROI createROI(boolean recordResult) {
		if (recordResult) {
			roi = polyline.croi;
		}
		return polyline.croi;
	}

	@Override
	protected void updateRegion() {
		if (polyline != null && roi instanceof PolylineROI) {
			polyline.updateFromROI((PolylineROI) roi);
			sync(getBean());
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 0; // signifies unlimited presses
	}

	@Override
	public void dispose() {
		super.dispose();
		if (polyline != null) {
			polyline.dispose();
		}
	}

	class Polyline extends Shape implements IRegionContainer {
		List<IFigure> handles;
		List<FigureTranslator> fTranslators;
		private Figure parent;
		private static final int SIDE = 8;
		private FigureListener moveListener;
		private Rectangle bnds;
		private ICoordinateSystem cs;
		private PolylineROI croi;
		private PointList points;

		public Polyline(Figure parent, ICoordinateSystem system) {
			super();
			this.parent = parent;
			cs = system;
			handles = new ArrayList<IFigure>();
			fTranslators = new ArrayList<FigureTranslator>();
			moveListener = new FigureListener() {
				@Override
				public void figureMoved(IFigure source) {
					Polyline.this.parent.repaint();
				}
			};
		}

		private final static int TOLERANCE = 2;

		@Override
		public boolean containsPoint(int x, int y) {
			if (croi == null)
				return super.containsPoint(x, y);
			return croi.isNearOutline(cs.getPositionValue(x, y), TOLERANCE);
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

		public void setup(PointList points) {
			croi = new PolylineROI();
			this.points = points;
			final Point p = new Point();
			boolean mobile = isMobile();
			boolean visible = isVisible() && mobile;
			Rectangle b = null;
			for (int i = 0, imax = points.size(); i < imax; i++) {
				points.getPoint(p, i);
				croi.insertPoint(cs.getPositionValue(p.x(), p.y()));
				Rectangle bh = addHandle(p.x, p.y(), mobile, visible);
				if (b == null) {
					b = new Rectangle(bh);
				} else {
					b.union(bh);
				}
			}

			addFigureListener(moveListener);
			FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, this, handles);
			mover.setActive(isMobile());
			mover.addTranslationListener(createRegionNotifier());
			fTranslators.add(mover);

			setRegionObjects(this, handles);
			if (b != null)
				setBounds(b);
		}

		private Rectangle updateFromHandles() {
			int i = 0;
			Rectangle b = null;
			for (IFigure f : handles) { // this is called first so update points
				if (f instanceof SelectionHandle) {
					SelectionHandle h = (SelectionHandle) f;
					Point pt = h.getSelectionPoint();
					points.setPoint(pt, i);
					croi.setPoint(i++, cs.getPositionValue(pt.x(), pt.y()));
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
			RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE, x, y);
			h.setVisible(visible);
			parent.add(h);
			FigureTranslator mover = new FigureTranslator(getXyGraph(), h);
			mover.setActive(mobile);
			mover.addTranslationListener(createRegionNotifier());
			fTranslators.add(mover);
			h.addFigureListener(moveListener);
			handles.add(h);
			return h.getBounds();
		}

		@Override
		public Rectangle getBounds() {
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
		 * @param proi
		 */
		public void updateFromROI(PolylineROI proi) {
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
				imax = proi.getNumberOfPoints();
				boolean mobile = isMobile();
				boolean visible = isVisible() && mobile;
				for (int i = 0; i < imax; i++) {
					PointROI r = proi.getPoint(i);
					if (proi != croi)
						croi.insertPoint(r);
					int[] pnt  = coords.getValuePosition(r.getPointRef());
					points.addPoint(pnt[0], pnt[1]);
					Rectangle hb = addHandle(pnt[0], pnt[1], mobile, visible);
					if (b == null) {
						b = new Rectangle(hb);
					} else {
						b.union(hb);
					}
				}
				addFigureListener(moveListener);
				FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, this, handles);
				mover.addTranslationListener(createRegionNotifier());
				mover.setActive(isMobile());
				fTranslators.add(mover);
				setRegionObjects(this, handles);
			} else {
				for (int i = 0; i < imax; i++) {
					PointROI p = proi.getPoint(i);
					if (proi != croi)
						croi.setPoint(i, p);
					int[] pnt = coords.getValuePosition(p.getPointRef());
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

		@Override
		public IRegion getRegion() {
			return PolylineSelection.this;
		}

		@Override
		public void setRegion(IRegion region) {
		}

		@Override
		protected void fillShape(Graphics graphics) {
			// do nothing
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			Rectangle b = getParent().getBounds();
			Draw2DUtils.drawClippedPolyline(graphics, points, b, false);
		}
	}
}
