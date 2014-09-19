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
import org.eclipse.dawnsci.analysis.dataset.roi.CircularFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
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

public class CircleFitSelection extends FitSelectionRegion<CircularFitROI> {
	private final static Logger logger = LoggerFactory.getLogger(CircleFitSelection.class);

	private static final int MIN_POINTS = 3; // minimum number of points to define ellipse

	public CircleFitSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.yellow);
	}

	@Override
	protected FitROIShape<CircularFitROI> createShape(Figure parent) {
		return parent == null ? new FRShape() : new FRShape(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.CIRCLEFIT;
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-circle.png";
	}

	private void updateLabel(CircularROI croi) {
		setLabel(String.format("%.2fpx", croi.getRadius()));
	}

	@Override
	protected void updateRegion() {
		super.updateRegion();
		if (roi instanceof CircularROI) {
			updateLabel((CircularROI) roi);
		}
	}

	@Override
	public int getMinimumMousePresses() {
		return MIN_POINTS;
	}

	class FRShape extends FitROIShape<CircularFitROI> {

		public FRShape() {
			super();
			setCoordinateSystem(coords);
		}

		public FRShape(Figure parent, AbstractSelectionRegion<CircularFitROI> region) {
			super(parent, region);
			setFill(false);
		}

		@Override
		protected ParametricROIHandler<CircularFitROI> createROIHandler(CircularFitROI roi) {
			return null;
		}

		@Override
		protected CircularFitROI createNewROI(IPolylineROI lroi) {
			try {
				return new CircularFitROI(lroi);
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
			graphics.setLineWidth(CircleFitSelection.this.getLineWidth());
			graphics.setForegroundColor(getRegionColor());
			graphics.setAlpha(CircleFitSelection.this.getAlpha());
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

		@Override
		public String toString() {
			double rad = cs.getPositionFromValue(getROI().getRadius())[0];
			return "CirSel: cen=" + getCentre() + ", rad=" + rad;
		}
	}
}
