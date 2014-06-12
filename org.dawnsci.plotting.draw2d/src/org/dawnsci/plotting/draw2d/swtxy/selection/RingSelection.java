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

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.api.region.ILockableRegion;
import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.dawnsci.plotting.draw2d.swtxy.util.PointFunction;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.RingROI;
import uk.ac.diamond.scisoft.analysis.roi.handler.ROIHandler;
import uk.ac.diamond.scisoft.analysis.roi.handler.RingROIHandler;

/**

                      ,,ggddY""""Ybbgg,,
                 ,agd""'    |         `""bg,
              ,gdP"         | 3           "Ybg,
            ,dP"            |                "Yb,
          ,dP"         _,,dd|"""Ybb,,_         "Yb,
         ,8"         ,dP"'  | |    `"Yb,         "8,
        ,8'        ,d"      | |2       "b,        `8,
       ,8'        d"        | |          "b        `8,
       d'        d'        ,gPPRg,        `b        `b
       8         8        dP'   `Yb        8         8
       8         8        8)  1  (8        8         8
       8         8        Yb     dP        8         8
       8         Y,        "8ggg8"        ,P         8
       Y,         Ya                     aP         ,P
       `8,         "Ya                 aP"         ,8'
        `8,          "Yb,_         _,dP"          ,8'
         `8a           `""YbbgggddP""'           a8'
          `Yba                                 adP'
            "Yba                             adY"
              `"Yba,                     ,adP"'
                 `"Y8ba,             ,ad8P"'
                      ``""YYbaaadPP""''
 *
 *
 *    1. Center 
 *    2. Inner Radius
 *    3. Outer Radius
 *
 * You should not call this concrete class outside of the draw2d extensions
 * unless absolutely required.
 */
class RingSelection extends ROISelectionRegion<RingROI> implements ILockableRegion {
	RingSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.yellow);
	}

	@Override
	protected ROIShape<RingROI> createShape(Figure parent) {
		return new RShape(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.RING;
	}

	private ROIShape<RingROI> tempShape;

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		if (clicks.size() < 2)
			return;

		g.setLineStyle(Graphics.LINE_DOT);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());

		final Point cen = clicks.getFirstPoint();
		Point inn = clicks.getPoint(1);
		Dimension rd = inn.getDifference(cen);
		double ratio = coords.getAspectRatio();
		double h = -rd.preciseHeight() / ratio;
		double w = rd.preciseWidth();
		final double ri = Math.hypot(w, h);
		if (clicks.size() == 2) {
			g.setLineWidth(getLineWidth());
			g.drawOval((int) Math.round(cen.preciseX() - ri), (int) Math.round(cen.preciseY() - ri * ratio),
					(int) Math.round(2 * ri), (int) Math.round(2 * ri * ratio));
		} else {
			if (tempShape == null) {
				tempShape = createShape(null);
			}
			tempShape.setup(clicks);
			tempShape.setLineStyle(Graphics.LINE_DOT);
			tempShape.setLineWidth(getLineWidth());
			tempShape.setForegroundColor(getRegionColor());
			tempShape.setBackgroundColor(getRegionColor());
			tempShape.paintFigure(g);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (tempShape != null) {
			tempShape.dispose();
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-circle.png";
	}

	@Override
	public int getMaximumMousePresses() {
		return 3;
	}

	class RShape extends ROIShape<RingROI> implements PointFunction {
		private PointFunction innerFunction;
		private PointFunction outerFunction;

		public RShape() {
			super();
		}

		public RShape(Figure parent, RingSelection sectorSelection) {
			super(parent, sectorSelection);
			setBackgroundColor(getRegionColor());

			innerFunction = new PointFunction.Stub() {
				@Override
				public Point calculatePoint(double... parameter) {
					return RShape.this.getPoint(parameter[0], 0);
				}
			};

			outerFunction = new PointFunction.Stub() {
				@Override
				public Point calculatePoint(double... parameter) {
					return RShape.this.getPoint(parameter[0], 1);
				}
			};
		}

		@Override
		protected ROIHandler<RingROI> createROIHandler(RingROI roi) {
			return new RingROIHandler(roi);
		}

		public void setCentre(Point nc) {
			double[] pt = cs.getValueFromPosition(nc.x(), nc.y());
			double[] pc = croi.getPointRef();
			pt[0] -= pc[0];
			pt[1] -= pc[1];
			croi.addPoint(pt);
			dirty = true;
			calcBox(croi, true);
		}

		@Override
		public void setup(PointList points) {
			final Point cen = points.getFirstPoint();
			double[] pc = cs.getValueFromPosition(cen.x(), cen.y());

			Point inn = points.getPoint(1);
			double[] pa = cs.getValueFromPosition(inn.x(), inn.y());
			pa[0] -= pc[0];
			pa[1] -= pc[1];
			final double ri = Math.hypot(pa[0], pa[1]);

			Point out = points.getPoint(2);
			double[] pb = cs.getValueFromPosition(out.x(), out.y());
			pb[0] -= pc[0];
			pb[1] -= pc[1];

			final double ro = Math.hypot(pb[0], pb[1]);
			croi = new RingROI(pc[0], pc[1], ri, ro);

			if (parent == null) { // for last click rendering
				return;
			}
			roiHandler.setROI(createROI(true));
			configureHandles();
		}

		/**
		 * Get point on ring at given angle
		 * 
		 * @param angle
		 *            (positive for anti-clockwise)
		 * @return
		 */
		public Point getPoint(double angle, int i) {
			RingROI sroi = getROI();
			if (sroi == null) {
				return null;
			}
			double r = sroi.getRadius(i);
			double[] c = sroi.getPointRef();
			double[] pt = cs.getPositionFromValue(c[0] + r * Math.cos(angle), c[1] + r * Math.sin(angle));
			return new PrecisionPoint(pt[0], pt[1]);
		}

		@Override
		public Point calculatePoint(double... parameter) {
			return null;
		}

		@Override
		public double[] calculateXIntersectionParameters(int x) {
			return null;
		}

		@Override
		public double[] calculateYIntersectionParameters(int y) {
			return null;
		}

		@Override
		protected void fillShape(Graphics graphics) {
			graphics.pushState();
			graphics.setAdvanced(true);
			graphics.setAntialias(SWT.ON);

			fillRing(graphics);
			graphics.popState();
		}

		private final static double TWO_PI = 2.0 * Math.PI;

		private void fillRing(Graphics graphics) {
			PointList points = Draw2DUtils.generateCurve(innerFunction, 0, TWO_PI);
			PointList oPoints = Draw2DUtils.generateCurve(outerFunction, 0, TWO_PI);
			oPoints.reverse();
			points.addAll(oPoints);
			graphics.fillPolygon(points);
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			graphics.pushState();
			graphics.setAdvanced(true);
			graphics.setAntialias(SWT.ON);

			Rectangle bnd = new Rectangle();
			graphics.getClip(bnd);
			PointList points = Draw2DUtils.generateCurve(innerFunction, 0, TWO_PI);
			Draw2DUtils.drawClippedPolyline(graphics, points, bnd, true);

			points = Draw2DUtils.generateCurve(outerFunction, 0, TWO_PI);
			Draw2DUtils.drawClippedPolyline(graphics, points, bnd, true);
			drawLabel(graphics, getROI().getPoint());
			graphics.popState();
		}

		@Override
		protected void calcBox(RingROI proi, boolean redraw) {
			RectangularROI rroi = proi.getBounds();
			double[] bp = cs.getPositionFromValue(rroi.getPointRef());
			double[] ep = cs.getPositionFromValue(rroi.getEndPoint());
			bnds = new Rectangle(new PrecisionPoint(bp[0], bp[1]), new PrecisionPoint(ep[0], ep[1]));
			ep = cs.getPositionFromValue(rroi.getPoint(0, 1));
			bnds.union(new PrecisionPoint(ep[0], ep[1]));
			ep = cs.getPositionFromValue(rroi.getPoint(1, 0));
			bnds.union(new PrecisionPoint(ep[0], ep[1]));
			if (redraw) {
				setBounds(bnds);
			}
			dirty = false;
		}

		public void setCentreHandleMoveable(boolean moveable) {
			fTranslators.get(0).setActive(moveable);
			handles.get(0).setVisible(moveable);
		}
	}

	private boolean isCentreMovable = true;

	@Override
	public boolean isCentreMovable() {
		return isCentreMovable;
	}

	@Override
	public void setCentreMovable(boolean isCentreMovable) {
		this.isCentreMovable = isCentreMovable;

		if (isCentreMovable) {
			((RShape) shape).setCentreHandleMoveable(true);
		} else {
			((RShape) shape).setCentreHandleMoveable(false);
		}
	}

	@Override
	public boolean isOuterMovable() {
		throw new RuntimeException("Cannot call isOuterMovable on " + getClass().getName());
	}

	@Override
	public void setOuterMovable(boolean isOuterMovable) {
		throw new RuntimeException("Cannot call setOuterMovable on " + getClass().getName());
	}
}
