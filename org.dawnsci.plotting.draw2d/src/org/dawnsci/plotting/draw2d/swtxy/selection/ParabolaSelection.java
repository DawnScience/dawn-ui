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
import org.dawnsci.plotting.api.region.ILockableRegion;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegionContainer;
import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationEvent;
import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationListener;
import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.dawnsci.plotting.draw2d.swtxy.util.PointFunction;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ParabolicROI;

class ParabolaSelection extends AbstractSelectionRegion implements ILockableRegion {

	Parabola parabola;

	ParabolaSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.orange);
		setAlpha(80);
		setLineWidth(2);
		labelColour = ColorConstants.black;
		if (labelFont != null)
			labelFont.dispose();
		labelFont = new Font(Display.getCurrent(), "Dialog", 10, SWT.BOLD);
	}

	@Override
	public void setMobile(boolean mobile) {
		super.setMobile(mobile);
		if (parabola != null)
			parabola.setMobile(mobile);
	}

	@Override
	public void createContents(Figure parent) {
		parabola = new Parabola(parent);
		parabola.setCoordinateSystem(coords);

		parent.add(parabola);
		sync(getBean());
		parabola.setLineWidth(getLineWidth());
	}

	@Override
	public boolean containsPoint(int x, int y) {
		return parabola.containsPoint(x, y);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.PARABOLA;
	}

	@Override
	protected void updateBounds() {
		if (parabola != null) {
			parabola.updateFromHandles();
			Rectangle b = parabola.getBounds();
			if (b != null)
				parabola.setBounds(b);
		}
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		if (clicks.size() <= 1)
			return;

		// FIXME
		g.setLineStyle(SWT.LINE_DOT);
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.drawOval(new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint()));
	}

	@Override
	public void initialize(PointList clicks) {
		if (parabola != null) {
			parabola.setup(clicks);
			fireROIChanged(getROI());
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-ellipse.png";
	}

	@Override
	protected IROI createROI(boolean recordResult) {
//		proi.setName(getName());
//		if (roi != null) {
//			proi.setPlot(roi.isPlot());
//			// set the Region isActive flag
//			this.setActive(roi.isPlot());
//		}
		if (recordResult) {
			roi = parabola.croi;
		}
		return parabola.croi;
	}

	@Override
	protected void updateRegion() {
		if (parabola != null && roi instanceof ParabolicROI) {
			parabola.updateFromROI((ParabolicROI) roi);
			sync(getBean());
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 2;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (parabola != null) {
			parabola.dispose();
		}
	}

	class Parabola extends Shape implements IRegionContainer, PointFunction {
		private boolean showMajorAxis;
		List<IFigure> handles;
		List<FigureTranslator> fTranslators;
		private Figure parent;
		private TranslationListener handleListener;
		private FigureListener moveListener;
		private static final int SIDE = 8;
		private Rectangle box;

		public Parabola(Figure parent) {
			super();
			handles = new ArrayList<IFigure>();
			fTranslators = new ArrayList<FigureTranslator>();
			this.parent = parent;
			setFill(false);
			handleListener = createHandleNotifier();
			showMajorAxis(true);
			moveListener = new FigureListener() {
				@Override
				public void figureMoved(IFigure source) {
					Parabola.this.parent.repaint();
				}
			};
		}

		private ICoordinateSystem cs;
		private ParabolicROI croi;

		@Override
		public void setCoordinateSystem(ICoordinateSystem system) {
			cs = system;
		}

		public ICoordinateSystem getCoordinateSystem() {
			return cs;
		}

		/**
		 * @param show if true, show major axis
		 */
		public void showMajorAxis(boolean show) {
			showMajorAxis = show;
		}

		public Point getFocus() {
			if (croi == null) {
				return null;
			}
			int[] pt = cs.getValuePosition(croi.getPointRef());
			return new Point(pt[0], pt[1]);
		}

		public void setFocus(int x, int y) {
			if (croi == null) {
				return;
			}
			double[] pt = cs.getPositionValue(x, y);
			croi.setPoint(pt);
		}

		/**
		 * Get point on ellipse at given angle
		 * @param angle (positive for anti-clockwise)
		 * @return
		 */
		public Point getPoint(double angle) {
			if (croi == null) {
				return null;
			}
			int[] pt = cs.getValuePosition(croi.getPoint(angle));
			return new Point(pt[0], pt[1]);
		}

		@Override
		public Point calculatePoint(double... parameter) {
			return getPoint(parameter[0]);
		}

		public double getAngleDegrees() {
			if (croi == null) {
				return 0;
			}
			return croi.getAngleDegrees();
		}

		public double getFocalParameter() {
			if (croi == null) {
				return 0;
			}
			return croi.getFocalParameter();
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

		public void setup(PointList corners) {
//			createHandles(true);
		}

		private static final int CENTRE = 4;

		private void createHandles(boolean createROI) {
//			boolean mobile = isMobile();
//			boolean visible = isVisible() && mobile;
//			// handles
//			for (int i = 0; i < 4; i++) {
//				addHandle(getPoint(i*90), mobile, visible);
//			}
//			addCentreHandle(mobile, visible); // centre should be fourth

			// figure move
			addFigureListener(moveListener);
			FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, this, handles){
				public void mouseDragged(MouseEvent event) {
					if (!isCentreMovable) return;
					super.mouseDragged(event);
				}
			};
			mover.setActive(isMobile());
			mover.addTranslationListener(createRegionNotifier());
			fTranslators.add(mover);

			if (createROI)
				createROI(true);

			setRegionObjects(this, handles);
			calcBox(false);
			Rectangle b = getBounds();
			if (b != null)
				setBounds(b);
		}

		@Override
		protected void fillShape(Graphics graphics) {
//			if (!isShapeFriendlySize()) return;
//
//			graphics.pushState();
//			graphics.setAdvanced(true);
//			graphics.setAntialias(SWT.ON);
//			graphics.translate((int) affine.getTranslationX(), (int) affine.getTranslationY());
//			graphics.rotate((float) affine.getRotationDegrees());
//			// NB do not use Graphics#scale and unit shape as there are precision problems
//			calcBox(false);
//			graphics.fillOval(0, 0, (int) affine.getScaleX(), (int) affine.getScaleY());
//			graphics.popState();
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			graphics.pushState();
			graphics.setAdvanced(true);
			graphics.setAntialias(SWT.ON);

			double max = getMaxRadius();
			double start = croi.getStartAngle(max);
			PointList points = Draw2DUtils.generateCurve(this, start, 2*Math.PI - start, Math.PI/100);
			Rectangle bnd = new Rectangle();
			graphics.getClip(bnd);
			Draw2DUtils.drawClippedPolyline(graphics, points, bnd, false);

			if (showMajorAxis) {
				double offset = Math.toRadians(cs.getXAxisRotationAngleDegrees());
				Point b = getPoint(Math.PI + offset);
				Point f = getFocus();
				Point e = b.getNegated().translate(f);
				double l = b.getDistance(f);
				e.scale((max + l)/l);
				graphics.drawLine(b, e.getTranslated(b));
			}
			graphics.popState();

			if (label != null && isShowLabel()) {
				graphics.setAlpha(192);
				graphics.setForegroundColor(labelColour);
				graphics.setBackgroundColor(ColorConstants.white);
				graphics.setFont(labelFont);
				graphics.fillString(label, getPoint(Math.PI * 0.75));
			}
		}

		public double getMaxRadius() {
			Point p = box.getTopLeft();
			double[] c = cs.getPositionValue(p.x(), p.y());
			double[] f = croi.getPointRef();
			double max = Math.hypot(c[0] - f[0], c[1] - f[1]);

			p = box.getTopRight();
			c = cs.getPositionValue(p.x(), p.y());
			max = Math.max(max, Math.hypot(c[0] - f[0], c[1] - f[1]));

			p = box.getBottomRight();
			c = cs.getPositionValue(p.x(), p.y());
			max = Math.max(max, Math.hypot(c[0] - f[0], c[1] - f[1]));

			p = box.getBottomLeft();
			c = cs.getPositionValue(p.x(), p.y());
			max = Math.max(max, Math.hypot(c[0] - f[0], c[1] - f[1]));
			return max;
		}

		private void calcBox(boolean redraw) {
			if (parent == null)
				return;
			box = parent.getBounds(); // as conic is unbounded, use parent for bounds
			if (redraw) {
				setBounds(box);
			}
		}

		public void setMobile(boolean mobile) {
			for (FigureTranslator f : fTranslators) {
				f.setActive(mobile);
			}
		}

		public void setCentreMobile(boolean mobile) {
			handles.get(CENTRE).setVisible(mobile);
			fTranslators.get(CENTRE).setActive(mobile);
		}

		public void setOuterMobile(boolean mobile) {
			for (int i = 0; i < CENTRE; i++) {
				handles.get(i).setVisible(mobile);
				fTranslators.get(i).setActive(mobile);
			}
		}

		private void addHandle(Point p, boolean mobile, boolean visible) {
			RectangularHandle h = new RectangularHandle(getCoordinateSystem(), getRegionColor(), this, SIDE,
					p.preciseX(), p.preciseY());
			h.setVisible(visible);
			parent.add(h);
			FigureTranslator mover = new FigureTranslator(getXyGraph(), h);
			mover.setActive(mobile);
			mover.addTranslationListener(handleListener);
			fTranslators.add(mover);
			h.addFigureListener(moveListener);
			handles.add(h);
		}

		private void addFocusHandle(boolean mobile, boolean visible) {
			Point c = getFocus();
			RectangularHandle h = new RectangularHandle(getCoordinateSystem(), getRegionColor(), this, SIDE, c.preciseX(), c.preciseY());
			h.setVisible(visible);
			parent.add(h);
			FigureTranslator mover = new FigureTranslator(getXyGraph(), h, h, handles);
			mover.setActive(mobile);
			mover.addTranslationListener(createRegionNotifier());
			fTranslators.add(mover);
			h.addFigureListener(moveListener);
			handles.add(h);
		}

		@Override
		public String toString() {
			return "ParaSel: focus=" + getFocus() + ", focal=" + getFocalParameter() + ", ang=" + getAngleDegrees();
		}

		private TranslationListener createHandleNotifier() {
			return new TranslationListener() {
				SelectionHandle ha;
				private boolean major;

				@Override
				public void onActivate(TranslationEvent evt) {
					Object src = evt.getSource();
					if (src instanceof FigureTranslator) {
						ha = (SelectionHandle) ((FigureTranslator) src).getRedrawFigure();
						int current = handles.indexOf(ha);
						switch (current) {
						case 0: case 2:
							major = true;
							break;
						case 1: case 3:
							major = false;
							break;
						default:
							ha = null;
							break;
						}
					}
				}

				@Override
				public void translateBefore(TranslationEvent evt) {
				}

				@Override
				public void translationAfter(TranslationEvent evt) {
//					Object src = evt.getSource();
//					if (src instanceof FigureTranslator) {
//						if (ha != null) {
//							double[] axes = getAxes();
//							Point pa = getInverseTransformedPoint(new PrecisionPoint(ha.getSelectionPoint()));
//							Dimension d = pa.getDifference(centre);
//							if (major) {
//								axes[0] *= 2. * Math.abs(d.preciseWidth());
//							} else {
//								axes[1] *= 2. * Math.abs(d.preciseHeight());
//							}
//							setAxes(axes[0], axes[1]);
//							updateHandlePositions();
//						}
//						fireROIDragged(createROI(false), ROIEvent.DRAG_TYPE.RESIZE);
//					}
				}

				@Override
				public void translationCompleted(TranslationEvent evt) {
					Object src = evt.getSource();
					if (src instanceof FigureTranslator) {
						fireROIChanged(createROI(true));
						fireROISelection();
					}
					ha = null;
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
					setFocus(p.x(), p.y());
				}
			}
		}

		@Override
		public Rectangle getBounds() {
			Rectangle b = box == null ? super.getBounds() : new Rectangle(box);
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
		 * @param proi
		 */
		public void updateFromROI(ParabolicROI proi) {
			this.croi = proi;
			updateHandlePositions();
		}

		private void updateHandlePositions() {
			final int imax = handles.size() - 1;
			if (imax > 0) {
				for (int i = 0; i < imax; i++) {
					Point np = getPoint(90 * i);
					SelectionHandle h = (SelectionHandle) handles.get(i);
					h.setSelectionPoint(np);
				}
				SelectionHandle h = (SelectionHandle) handles.get(imax);
				h.setSelectionPoint(getFocus());
			} else {
				createHandles(false);
			}
		}

		@Override
		public IRegion getRegion() {
			return ParabolaSelection.this;
		}

		@Override
		public void setRegion(IRegion region) {
		}
	}

	private boolean isCentreMovable = true;
	private boolean isOuterMovable = true;

	@Override
	public boolean isCentreMovable() {
		return isCentreMovable;
	}

	@Override
	public void setCentreMovable(boolean isCenterMovable) {
		this.isCentreMovable = isCenterMovable;
		parabola.setCursor(isCenterMovable ? Draw2DUtils.getRoiMoveCursor() : null);
		parabola.setCentreMobile(isCenterMovable);
	}

	@Override
	public boolean isOuterMovable() {
		return isOuterMovable;
	}

	@Override
	public void setOuterMovable(boolean isOuterMovable) {
		this.isOuterMovable = isOuterMovable;
		if (isOuterMovable)
			parabola.setCursor(Draw2DUtils.getRoiMoveCursor());
		else
			parabola.setCursor(null);

		parabola.setOuterMobile(isOuterMovable);
	}
}
