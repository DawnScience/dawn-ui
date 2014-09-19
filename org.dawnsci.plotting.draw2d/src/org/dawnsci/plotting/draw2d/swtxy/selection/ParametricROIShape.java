/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.dawnsci.plotting.draw2d.swtxy.util.PointFunction;
import org.eclipse.dawnsci.analysis.api.roi.IParametricROI;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * Class for a shape whose outline is defined parametrically 
 */
abstract public class ParametricROIShape<T extends IParametricROI> extends ParametricROIShapeBase<T> implements PointFunction {
	protected boolean showMajorAxis;

	public ParametricROIShape() {
		super();
	}

	public ParametricROIShape(Figure parent, AbstractSelectionRegion<T> region) {
		super(parent, region);
	}

	/**
	 * @param show if true, show major axis
	 */
	public void showMajorAxis(boolean show) {
		showMajorAxis = show;
	}

	private double getMaxRadius(Rectangle parentBounds) {
		Point p = parentBounds.getTopLeft();
		double[] c = cs.getValueFromPosition(p.x(), p.y());
		double[] f = getROI().getPointRef();
		double max = Math.hypot(c[0] - f[0], c[1] - f[1]);

		p = parentBounds.getTopRight();
		c = cs.getValueFromPosition(p.x(), p.y());
		max = Math.max(max, Math.hypot(c[0] - f[0], c[1] - f[1]));

		p = parentBounds.getBottomRight();
		c = cs.getValueFromPosition(p.x(), p.y());
		max = Math.max(max, Math.hypot(c[0] - f[0], c[1] - f[1]));

		p = parentBounds.getBottomLeft();
		c = cs.getValueFromPosition(p.x(), p.y());
		max = Math.max(max, Math.hypot(c[0] - f[0], c[1] - f[1]));
		return max;
	}

	protected void outlineShape(Graphics graphics, Rectangle parentBounds, boolean isClosed) {
		graphics.pushState();
		graphics.setAdvanced(true);
		graphics.setAntialias(SWT.ON);

		double max = isClosed ? 0 : getMaxRadius(parentBounds);
		T lroi = getROI();
		double start = lroi.getStartParameter(max);
		double end = lroi.getEndParameter(max);
		if (isClosed) {
			PointList points = Draw2DUtils.generateCurve(this, start, end);
			Draw2DUtils.drawClippedPolyline(graphics, points, parentBounds, isClosed);
		} else {
			if (!Draw2DUtils.drawCurve(graphics, parentBounds, false, this, start, end)) {
				graphics.popState();
				return;
			}
		}

		if (showMajorAxis) {
			Point b = getPoint(Math.PI);
			Point e;
			if (isClosed) {
				e = getPoint(start);
			} else {
				// by symmetry, use midpoint between max radius
				do { // expand circle until that point is outside bounds
					max *= 1.5; 
					start = lroi.getStartParameter(max);
					end = lroi.getEndParameter(max);
					e = getPoint(start);
					e.translate(getPoint(end));
					e.scale(0.5);
				} while (parentBounds.contains(e));
			}
			graphics.drawLine(b, e);
		}
		graphics.popState();
	}

	/**
	 * Override this to fill
	 * @param graphics
	 * @param parentBounds
	 */
	@Override
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

	public void setCentreMobile(boolean mobile) {
		handles.get(0).setVisible(mobile);
		fTranslators.get(0).setActive(mobile);
	}

	public void setOuterMobile(boolean mobile) {
		for (int i = 1, imax = handles.size(); i < imax; i++) {
			handles.get(i).setVisible(mobile);
			fTranslators.get(i).setActive(mobile);
		}
	}
}
