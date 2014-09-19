/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.draw2d.swtxy.util.PointFunction;
import org.eclipse.dawnsci.analysis.api.roi.IParametricROI;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Base class for a shape whose outline is defined parametrically 
 */
abstract public class ParametricROIShapeBase<T extends IParametricROI> extends ROIShape<T> implements PointFunction {
	private int tolerance = 2;
	private boolean outlineOnly = true;

	public ParametricROIShapeBase() {
		super();
	}

	public ParametricROIShapeBase(Figure parent, AbstractSelectionRegion<T> region) {
		super(parent, region);
	}

	@Override
	public void setFill(boolean b) {
		super.setFill(b);
		outlineOnly = !b;
	}

	/**
	 * Set tolerance of hit detection of shape
	 * @param tolerance (number of pixels between point and segment)
	 */
	public void setTolerance(int tolerance) {
		this.tolerance = tolerance;
	}

	@Override
	public boolean containsPoint(int x, int y) {
		if (croi == null)
			return super.containsPoint(x, y);

		double[] pt = cs.getValueFromPosition(x, y);
		if (outlineOnly) {
			return croi.isNearOutline(pt[0], pt[1], tolerance);
		}

		return croi.containsPoint(pt[0], pt[1]);
	}

	/**
	 * Get point on ROI
	 * @param parameter
	 * @return point
	 */
	public Point getPoint(double parameter) {
		T lroi = getROI();
		if (lroi == null) {
			return null;
		}
		double[] pt = cs.getPositionFromValue(lroi.getPoint(parameter));
		return new PrecisionPoint(pt[0], pt[1]);
	}

	@Override
	public Point calculatePoint(double... parameter) {
		return getPoint(parameter[0]);
	}

	@Override
	public double[] calculateXIntersectionParameters(int x) {
		T lroi = getROI();
		if (lroi == null) {
			return null;
		}
		double dx = cs.getValueFromPosition(x, 0)[0];
		return lroi.getVerticalIntersectionParameters(dx);
	}

	@Override
	public double[] calculateYIntersectionParameters(int y) {
		T lroi = getROI();
		if (lroi == null) {
			return null;
		}
		double dy = cs.getValueFromPosition(0, y)[1];
		return lroi.getHorizontalIntersectionParameters(dy);
	}

	protected Point getCentre() {
		T lroi = getROI();
		if (lroi == null) {
			return null;
		}
		double[] pt = cs.getPositionFromValue(lroi.getPointRef());
		return new PrecisionPoint(pt[0], pt[1]);
	}

	@Override
	public void setCentre(Point nc) {
		T lroi = getROI();
		if (lroi == null) {
			return;
		}
		double[] pt = cs.getValueFromPosition(nc.x(), nc.y());
		double[] pc = lroi.getPointRef();
		pt[0] -= pc[0];
		pt[1] -= pc[1];
		lroi.addPoint(pt);
		dirty = true;
		calcBox(lroi, true);
	}

	abstract protected void outlineShape(Graphics graphics, Rectangle parentBounds);

	/**
	 * Override this to fill
	 * @param graphics
	 * @param parentBounds
	 */
	protected void fillShape(Graphics graphics, Rectangle parentBounds) {
//		PointList points = Draw2DUtils.generateCurve(this, croi.getStartParameter(0), croi.getEndParameter(0));
//		graphics.fillPolygon(points);
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		outlineShape(graphics, bnds);
	}

	@Override
	protected void fillShape(Graphics graphics) {
		fillShape(graphics, bnds);
	}

	abstract public void setup(PointList corners, boolean withHandles);

	@Override
	public void setup(PointList corners) {
		setup(corners, true);
	}
}
