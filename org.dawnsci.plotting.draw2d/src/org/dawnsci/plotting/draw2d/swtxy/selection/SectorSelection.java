/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.draw2d.swtxy.IMobileFigure;
import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.dawnsci.plotting.draw2d.swtxy.util.PointFunction;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ROIHandler;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.SectorROIHandler;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.region.ILockableRegion;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * You should not call this concrete class outside of the draw2d 
 * extensions unless absolutely required.
 */
class SectorSelection extends ROISelectionRegion<SectorROI> implements ILockableRegion {

	SectorSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.red);
	}

	@Override
	protected ROIShape<SectorROI> createShape(Figure parent) {
		return new Sector(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.SECTOR;
	}

	private ROIShape<SectorROI> tempShape;

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
					(int) Math.round(2*ri), (int) Math.round(2*ri*ratio));
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
	protected SectorROI createROI(boolean recordResult) {
		return super.createROI(recordResult);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (tempShape != null) {
			tempShape.dispose();
		}
	}

	private Boolean clockwise = null;
	private final static double ONE_PI = Math.PI;
	private final static double TWO_PI = 2.0 * Math.PI;
	private double[] calcAngles(double anglea, double angleb) {
		if (anglea < 0)
			anglea += TWO_PI;
		if (angleb < 0)
			angleb += TWO_PI;
		if (clockwise == null) {
			if (anglea == 0) {
				clockwise = angleb > ONE_PI;
			} else {
				clockwise = anglea > angleb;
			}
		}

		double l;
		if (clockwise) {
			if (anglea < ONE_PI) {
				if (angleb < ONE_PI) {
					l = angleb - anglea;
					if (l > 0)
						l -= TWO_PI;
				} else
					l = angleb - TWO_PI - anglea;
			} else {
				if (angleb < ONE_PI) {
					l = angleb - anglea;
				} else {
					l = angleb - anglea;
					if (l > 0)
						l -= TWO_PI;
				}
			}
		} else {
			if (anglea < ONE_PI) {
				if (angleb < ONE_PI) {
					l = angleb - anglea;
					if (l < 0)
						l += TWO_PI;
				} else
					l = angleb - anglea;
			} else {
				if (angleb < ONE_PI)
					l = angleb - anglea + TWO_PI;
				else {
					l = angleb - anglea;
					if (l < 0)
						l += TWO_PI;
				}
			}
		}

		return l < 0 ? new double[] {anglea + l, anglea} : new double[] {anglea, anglea + l};
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-sector.png";
	}

	@Override
	public int getMaximumMousePresses() {
		return 3;
	}

	class Sector extends ROIShape<SectorROI> implements PointFunction {
		private PointFunction innerFunction;
		private PointFunction outerFunction;

		public Sector() {
			super();
		}

		public Sector(Figure parent, SectorSelection sectorSelection) {
			super(parent, sectorSelection);
			setBackgroundColor(getRegionColor());

			innerFunction = new PointFunction.Stub() {
				@Override
				public Point calculatePoint(double... parameter) {
					return Sector.this.getPoint(parameter[0], 0);
				}
			};

			outerFunction = new PointFunction.Stub() {
				@Override
				public Point calculatePoint(double... parameter) {
					return Sector.this.getPoint(parameter[0], 1);
				}
			};
		}

		@Override
		protected ROIHandler<SectorROI> createROIHandler(SectorROI roi) {
			return new SectorROIHandler(roi);
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
			double as = Math.atan2(pa[1], pa[0]);
			final double ri = Math.hypot(pa[0], pa[1]);

			Point out = points.getPoint(2);
			double[] pb = cs.getValueFromPosition(out.x(), out.y());
			pb[0] -= pc[0];
			pb[1] -= pc[1];

			double ae = Math.atan2(pb[1], pb[0]);
			final double ro = Math.hypot(pb[0], pb[1]);
			double[] a = calcAngles(as, ae);
			croi = new SectorROI(pc[0], pc[1], ri, ro, a[0], a[1]);

			if (parent == null) { // for last click rendering
				return;
			}
			roiHandler.setROI(createROI(true));
			configureHandles();
		}

		/**
		 * Get point on sector at given angle
		 * @param angle (positive for anti-clockwise)
		 * @return
		 */
		public Point getPoint(double angle, int i) {
			SectorROI sroi = getROI();
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

			SectorROI sroi = getROI();
			fillSector(graphics, sroi.getAngles());

			if (sroi.getSymmetry() != SectorROI.NONE)
				fillSector(graphics, sroi.getSymmetryAngles());

			graphics.popState();
		}

		private void fillSector(Graphics graphics, double[] ang) {
			PointList points = Draw2DUtils.generateCurve(innerFunction, ang[0], ang[1]);
			PointList oPoints = Draw2DUtils.generateCurve(outerFunction, ang[0], ang[1]);
			oPoints.reverse();
			points.addAll(oPoints);
			graphics.fillPolygon(points);
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			graphics.pushState();
			graphics.setAdvanced(true);
			graphics.setAntialias(SWT.ON);

			double[] ang = getROI().getAngles();
			PointList points = Draw2DUtils.generateCurve(innerFunction, ang[0], ang[1]);
			PointList oPoints = Draw2DUtils.generateCurve(outerFunction, ang[0], ang[1]);
			oPoints.reverse();
			points.addAll(oPoints);
			Rectangle bnd = new Rectangle();
			graphics.getClip(bnd);
			Draw2DUtils.drawClippedPolyline(graphics, points, bnd, true);

			graphics.setForegroundColor(ColorConstants.black);
			graphics.setAlpha(255);
			
			if (isShowLabel()) {
				try {
					graphics.drawString(getName(), ((SelectionHandle)handles.get(4)).getSelectionPoint());
				} catch (IndexOutOfBoundsException ignored) {
					// Ok no label then.
				}
			}
			
			graphics.popState();
		}

		@Override
		protected void calcBox(SectorROI proi, boolean redraw) {
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
			fTranslators.get(9).setActive(moveable);
			handles.get(9).setVisible(moveable);
		}

		@Override
		public void snapToGrid() {
			SectorROI cSnappedROI = croi;
			if (cSnappedROI != null) {
				cSnappedROI.setPoint(Math.round(cSnappedROI.getPoint()[0]), Math.round(cSnappedROI.getPoint()[1]));
				cSnappedROI.setRadii(new double[] {Math.round(cSnappedROI.getRadii()[0]), Math.round(cSnappedROI.getRadii()[1])});
				cSnappedROI.setDpp(Math.round(cSnappedROI.getDpp()));
				croi = cSnappedROI;
			}
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
		((Sector) shape).setCentreHandleMoveable(isCentreMovable);
	}
	
	@Override
	public void setMobile(boolean mobile) {
		
		bean.setMobile(mobile);
		if (!bean.isVisible()) return;

		if (regionObjects != null) {
			for (IFigure ob : regionObjects) {
				if (ob instanceof IMobileFigure) {
					
					if (mobile != ob.isVisible()) {
						if (ob == ((Sector) shape).handles.get(9)) {
						    ob.setVisible(mobile&&isCentreMovable);
						} else {
						    ob.setVisible(mobile);
						}
					}
						
				} else if (ob instanceof RegionFillFigure) {
					if (((RegionFillFigure<?>) ob).isMobile() != mobile)
						((RegionFillFigure<?>) ob).setMobile(mobile);
				}
			}
		}
	}

	@Override
	public boolean isOuterMovable() {
		throw new RuntimeException("Cannot call isOuterMovable on "+getClass().getName());
	}

	@Override
	public void setOuterMovable(boolean isOuterMovable) {
		throw new RuntimeException("Cannot call setOuterMovable on "+getClass().getName());
	}
}
