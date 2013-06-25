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
import org.dawnsci.plotting.api.region.IEllipseFitSelection;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegionContainer;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.draw2d.swtxy.RegionBean;
import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationEvent;
import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationListener;
import org.dawnsci.plotting.draw2d.swtxy.util.RotatableEllipse;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.fitting.CircleFitter;
import uk.ac.diamond.scisoft.analysis.fitting.EllipseFitter;
import uk.ac.diamond.scisoft.analysis.fitting.IConicSectionFitter;
import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;

class EllipseFitSelection extends AbstractSelectionRegion implements IEllipseFitSelection {
	private final static Logger logger = LoggerFactory.getLogger(EllipseFitSelection.class);

	private static final int CIR_POINTS = 3; // minimum number of points to define circle
	private static final int ELL_POINTS = 5; // minimum number of points to define ellipse
	DecoratedEllipse ellipse;
	private IConicSectionFitter eFitter;
	private CircleFitter cFitter;

	public EllipseFitSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.green);
		setAlpha(80);
		setLineWidth(2);
		eFitter = new EllipseFitter();
		cFitter = new CircleFitter();
	}

	@Override
	public void setVisible(boolean visible) {
		getBean().setVisible(visible);
		if (ellipse != null)
			ellipse.setVisible(visible);
	}

	@Override
	public void setMobile(final boolean mobile) {
		getBean().setMobile(mobile);
		if (ellipse != null)
			ellipse.setMobile(mobile);
	}

	@Override
	public void createContents(Figure parent) {
		ellipse = new DecoratedEllipse(parent);
		ellipse.setCoordinateSystem(coords);
		//ellipse.setCursor(Draw2DUtils.getRoiMoveCursor());

		parent.add(ellipse);
		sync(getBean());
		ellipse.setForegroundColor(getRegionColor());
		ellipse.setAlpha(getAlpha());
		ellipse.setLineWidth(getLineWidth());
		updateROI();
		if (roi == null)
			createROI(true);
	}

	@Override
	public boolean containsPoint(double x, double y) {
		final int[] pix = coords.getValuePosition(x,y);
		return ellipse.containsPoint(pix[0], pix[1]);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.ELLIPSEFIT;
	}

	@Override
	protected void updateConnectionBounds() {
		if (ellipse != null) {
			ellipse.updateFromHandles();
			Rectangle b = ellipse.getBounds();
			if (b != null)
				ellipse.setBounds(b);
		}
	}

	private boolean circleOnly = false;

	private boolean fitPoints(PointList pts, RotatableEllipse ellipse) {
		if (pts == null)
			return false;

		final int n = pts.size();
		final double[] x = new double[n];
		final double[] y = new double[n];
		final Point p = new Point();
		for (int i = 0; i < n; i++) {
			pts.getPoint(p, i);
			double[] c = coords.getPositionValue(p.x(), p.y());
			x[i] = c[0];
			y[i] = c[1];
		}
		AbstractDataset xds = new DoubleDataset(x, n); 
		AbstractDataset yds = new DoubleDataset(y, n);

		try {
			int[] pnt1;
			int[] pnt2;
			double ang;
			if (circleOnly || (n >= CIR_POINTS && n < ELL_POINTS)) {
				cFitter.geometricFit(xds, yds, null);
				final double[] parameters = cFitter.getParameters();
				pnt1 = coords.getValuePosition(2 * parameters[0] + parameters[1], 2 * parameters[0] + parameters[2]);
				pnt2 = coords.getValuePosition(parameters[1], parameters[2]);
				ang = 0;
			} else {
				eFitter.geometricFit(xds, yds, null);
				final double[] parameters = eFitter.getParameters();
				pnt1 = coords.getValuePosition(2 * parameters[0] + parameters[3], 2 * parameters[1] + parameters[4]);
				pnt2 = coords.getValuePosition(parameters[3], parameters[4]);
				ang = Math.toDegrees(parameters[2]);
			}
			double ratio = coords.getAspectRatio();
			ellipse.setAxes(pnt1[0] - pnt2[0], (pnt1[1] - pnt2[1])/ratio);
			ellipse.setCentre(pnt2[0], pnt2[1]);
			ellipse.setAngleDegrees(ang);
		} catch (IllegalArgumentException e) {
			logger.info("Can not fit current selection");
			return false;
		}
		return true;
	}

	private RotatableEllipse tempEllipse;

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.setLineStyle(Graphics.LINE_DOT);
		g.drawPolyline(clicks);
		if (clicks.size() >= CIR_POINTS) {
			if (tempEllipse == null) {
				tempEllipse = new RotatableEllipse();
				tempEllipse.setCoordinateSystem(coords);
				tempEllipse.setOutline(true);
				tempEllipse.setFill(false);
			}
			if (fitPoints(clicks, tempEllipse)) {
				tempEllipse.setVisible(true);
				tempEllipse.paintFigure(g);
			} else {
				tempEllipse.setVisible(false);
			}
		}
	}

	@Override
	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
		if (ellipse != null) {
			ellipse.setup(clicks);
			setRegionColor(getRegionColor());
			setOpaque(false);
			setAlpha(getAlpha());
			updateConnectionBounds();
			fireROIChanged(getROI());
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-ellipse.png";
	}

	@Override
	protected IROI createROI(boolean recordResult) {
		final PointList pl = ellipse.getPoints();
		if (pl == null) {
			return null;
		}

		final PolylineROI hroi = new PolylineROI();
		final Point p = new Point();
		for (int i = 0, imax = pl.size(); i < imax; i++) {
			pl.getPoint(p, i);
			hroi.insertPoint(i, coords.getPositionValue(p.x(), p.y()));
		}

		try {
			final EllipticalFitROI eroi = new EllipticalFitROI(hroi, circleOnly);
			if (roi!=null) eroi.setPlot(roi.isPlot());
			// set the Region isActive flag
			this.setActive(this.isActive());
			if (recordResult) {
				roi = eroi;
			}
			return eroi;
		} catch (IllegalArgumentException e) {
			// do nothing
		}
		return roi;
	}

	@Override
	protected void updateROI(IROI roi) {
		if (ellipse == null)
			return;

		if (roi instanceof PolylineROI && !(roi instanceof EllipticalFitROI)) {
			roi = new EllipticalFitROI((PolylineROI)roi);
		}
		
		if (roi instanceof EllipticalFitROI) {
			ellipse.updateFromROI((EllipticalFitROI) roi);
			updateConnectionBounds();
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 0; // signifies unlimited presses
	}

	@Override
	public int getMinimumMousePresses() {
		return CIR_POINTS;
	}

	/**
	 * Set whether fit is restricted to a circle
	 * @param fitCircle if true, then fit to circle
	 */
	public void setFitCircle(boolean fitCircle) {
		boolean renew = circleOnly ^ fitCircle;
		circleOnly = fitCircle;
		if (renew && ellipse != null) {
			createROI(true);
			updateROI(roi);
			fireROIChanged(roi);
		}
	}

	class DecoratedEllipse extends RotatableEllipse implements IRegionContainer {
		List<IFigure> handles;
		List<FigureTranslator> fTranslators;
		private Figure parent;
		private TranslationListener handleListener;
		private FigureListener moveListener;
		private static final int SIDE = 8;
		private static final double HALF_SIDE = 8/2;

		public DecoratedEllipse(Figure parent) {
			super();
			handles = new ArrayList<IFigure>();
			fTranslators = new ArrayList<FigureTranslator>();
			this.parent = parent;
			setFill(false);
			handleListener = createHandleNotifier();
			moveListener = new FigureListener() {
				@Override
				public void figureMoved(IFigure source) {
					DecoratedEllipse.this.parent.repaint();
				}
			};

			showMajorAxis(true);
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

		public void setup(PointList points) {
			fitPoints(points, this);

			// handles
			final Point p = new Point();
			for (int i = 0, imax = points.size(); i < imax; i++) {
				points.getPoint(p, i);
				addHandle(p);
			}
			addCentreHandle();

			// figure move (commented out for usability when tweaking handles)
//			addFigureListener(moveListener);
//			FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, this, handles);
//			mover.addTranslationListener(createRegionNotifier());

			createROI(true);

			setRegionObjects(this, handles);
			Rectangle b = getBounds();
			if (b != null)
				setBounds(b);
		}

		private void addHandle(Point p) {
			RectangularHandle h = new RectangularHandle(getCoordinateSystem(), getRegionColor(), this, SIDE,
					p.preciseX() - HALF_SIDE, p.preciseY() - HALF_SIDE);
			parent.add(h);
			FigureTranslator mover = new FigureTranslator(getXyGraph(), h);
			mover.addTranslationListener(handleListener);
			fTranslators.add(mover);
			h.addFigureListener(moveListener);
			handles.add(h);
		}

		private void addCentreHandle() {
			Point c = getCentre();
			RectangularHandle h = new RectangularHandle(getCoordinateSystem(), getRegionColor(), this, SIDE,
					c.preciseX() - HALF_SIDE, c.preciseY() - HALF_SIDE);
			parent.add(h);
			FigureTranslator mover = new FigureTranslator(getXyGraph(), h, h, handles);
			mover.addTranslationListener(createRegionNotifier());
			fTranslators.add(mover);
			h.addFigureListener(moveListener);
			handles.add(h);
		}

		private void removeHandle(SelectionHandle h) {
			parent.remove(h);
			h.removeFigureListener(moveListener);
			h.removeMouseListeners();
		}

		private TranslationListener createHandleNotifier() {
			return new TranslationListener() {
				@Override
				public void onActivate(TranslationEvent evt) {
				}

				@Override
				public void translateBefore(TranslationEvent evt) {
				}

				@Override
				public void translationAfter(TranslationEvent evt) {
					Object src = evt.getSource();
					if (src instanceof FigureTranslator) {
						if (fitPoints(getPoints(), DecoratedEllipse.this)) {
							if (handles.size() > 0) {
								IFigure f = handles.get(handles.size() - 1);
								if (f instanceof SelectionHandle) {
									SelectionHandle h = (SelectionHandle) f;
									h.setSelectionPoint(getCentre());
								}
							}
							fireROIDragged(createROI(false), ROIEvent.DRAG_TYPE.RESIZE);
						}
					}
				}

				@Override
				public void translationCompleted(TranslationEvent evt) {
					Object src = evt.getSource();
					if (src instanceof FigureTranslator) {
						fireROIChanged(createROI(true));
						fireROISelection();
					}
				}
			};
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

		/**
		 * Update selection according to centre handle
		 */
		private void updateFromHandles() {
			if (handles.size() > 0) {
				IFigure f = handles.get(handles.size() - 1);
				if (f instanceof SelectionHandle) {
					SelectionHandle h = (SelectionHandle) f;
					Point p = h.getSelectionPoint();
					setCentre(p.preciseX(), p.preciseY());
					double[] parameters = eFitter.getParameters();
					double[] ps   = getCoordinateSystem().getPositionValue(p.x(), p.y());
					parameters[3] = ps[0];
					parameters[4] = ps[1];
				}
			}
		}

		@Override
		public Rectangle getBounds() {
			Rectangle b = super.getBounds();
			if (handles != null)
				for (IFigure f : handles) {
					if (f instanceof SelectionHandle) {
						SelectionHandle h = (SelectionHandle) f;
						b.union(h.getBounds());
					}
				}
			return b;
		}

		/**
		 * Update according to ROI
		 * @param sroi
		 */
		public void updateFromROI(EllipticalFitROI eroi) {
			final double[] xy = eroi.getPointRef();
			int[] p1 = getCoordinateSystem().getValuePosition(xy[0], xy[1]);
			int[] p2 = getCoordinateSystem().getValuePosition(2*eroi.getSemiAxis(0) + xy[0], 2*eroi.getSemiAxis(1) + xy[1]);

			setAxes(p2[0] - p1[0], (p2[1] - p1[1])/getAspectRatio());

			setCentre(p1[0], p1[1]);
			setAngleDegrees(eroi.getAngleDegrees());

			int imax = handles.size() - 1;
			PolylineROI proi = eroi.getPoints();

			if (imax != proi.getNumberOfPoints()) {
				for (int i = imax; i >= 0; i--) {
					removeHandle((SelectionHandle) handles.remove(i));
					fTranslators.remove(i).removeTranslationListeners();
				}
				imax = proi.getNumberOfPoints();
				for (int i = 0; i < imax; i++) {
					PointROI p = proi.getPoint(i);
					int[] pos = getCoordinateSystem().getValuePosition(p.getPoint());
					Point np = new Point(pos[0], pos[1]);
					addHandle(np);
				}
				addCentreHandle();
				setRegionObjects(this, handles);
				RegionBean b = getBean();
				setVisible(b.isVisible());
				setMobile(b.isMobile());
			} else {
				for (int i = 0; i < imax; i++) {
					PointROI p = proi.getPoint(i);
					int[] pos = getCoordinateSystem().getValuePosition(p.getPoint());
					Point np = new Point(pos[0], pos[1]);
					SelectionHandle h = (SelectionHandle) handles.get(i);
					h.setSelectionPoint(np);
				}
				SelectionHandle h = (SelectionHandle) handles.get(imax);
				h.setSelectionPoint(getCentre());
			}
		}

		@Override
		public IRegion getRegion() {
			return EllipseFitSelection.this;
		}

		@Override
		public void setRegion(IRegion region) {
		}
	}

}
