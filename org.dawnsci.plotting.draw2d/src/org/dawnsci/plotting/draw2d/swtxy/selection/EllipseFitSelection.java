/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.dawnsci.analysis.api.roi.IPolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ParametricROIHandler;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EllipseFitSelection extends FitSelectionRegion<EllipticalFitROI> {
	private final static Logger logger = LoggerFactory.getLogger(EllipseFitSelection.class);

	private static final int MIN_POINTS = 3; // minimum number of points to define circle

	public EllipseFitSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.green);
	}

	@Override
	protected FitROIShape<EllipticalFitROI> createShape(Figure parent) {
		return parent == null ? new FRShape() : new FRShape(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.ELLIPSEFIT;
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-ellipse.png";
	}

	@Override
	public int getMinimumMousePresses() {
		return MIN_POINTS;
	}

	private boolean circleOnly = false;

	/**
	 * Set whether fit is restricted to a circle
	 * @param fitCircle if true, then fit to circle
	 */
	public void setFitCircle(boolean fitCircle) {
		boolean renew = circleOnly ^ fitCircle;
		circleOnly = fitCircle;
		if (renew && shape != null) {
			createROI(true);
			updateRegion();
			fireROIChanged(roi);
		}
	}

	class FRShape extends FitROIShape<EllipticalFitROI> {
		public FRShape() {
			super();
			setCoordinateSystem(coords);
		}

		public FRShape(Figure parent, AbstractSelectionRegion<EllipticalFitROI> region) {
			super(parent, region);
			setFill(false);
		}

		@Override
		protected ParametricROIHandler<EllipticalFitROI> createROIHandler(EllipticalFitROI roi) {
			return null;
		}

		@Override
		protected EllipticalFitROI createNewROI(IPolylineROI lroi) {
			try {
				return new EllipticalFitROI(lroi, circleOnly);
			} catch (Exception e) {
				logger.debug("Could not fit points", e);
				// do nothing
			}
			return null;
		}

		@Override
		protected void fillShape(Graphics graphics, Rectangle parentBounds) {
		}
		
		@Override
		protected void outlineShape(Graphics graphics, Rectangle parentBounds) {
			graphics.setLineWidth(EllipseFitSelection.this.getLineWidth());
			graphics.setForegroundColor(getRegionColor());
			graphics.setAlpha(EllipseFitSelection.this.getAlpha());
			outlineShape(graphics, parentBounds, true);

			if (label != null && isShowLabel()) {
				graphics.setLineStyle(SWT.LINE_DASH);
				Point pa = getPoint(0.75 * Math.PI);
				Point pb = getCentre();
				graphics.drawLine(pa, pb);
				graphics.setAlpha(192);
				graphics.setBackgroundColor(ColorConstants.white);
				graphics.setForegroundColor(labelColour);
				graphics.setFont(labelFont);
				graphics.fillString(label, pa.translate(pb).scale(0.5).translate(0, -labeldim.height));
			}
		}
	}
}
