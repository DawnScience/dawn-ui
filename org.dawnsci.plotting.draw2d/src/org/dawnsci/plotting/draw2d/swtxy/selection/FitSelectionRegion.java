/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.dawnsci.analysis.api.roi.IFitROI;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

abstract class FitSelectionRegion<T extends IFitROI> extends ROISelectionRegion<T> {

	FitSelectionRegion(String name, ICoordinateSystem coords) {
		super(name, coords);
		labelColour = ColorConstants.black;
	}

	@Override
	abstract protected FitROIShape<T> createShape(Figure parent);

	private FitROIShape<T> tempShape = null;

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.setLineStyle(Graphics.LINE_DOT);
		g.drawPolyline(clicks);
		if (clicks.size() >= getMinimumMousePresses()) {
			if (tempShape == null) {
				tempShape = createShape(null);
				tempShape.setOutline(true);
				tempShape.setFill(false);
			}
			tempShape.setup(clicks, false);
			if (tempShape.isFitted()) {
				tempShape.setVisible(true);
				tempShape.paintFigure(g);
			} else {
				tempShape.setVisible(false);
			}
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 0;
	}

	@Override
	public void initialize(PointList clicks) {
		if (tempShape != null) {
			tempShape.setVisible(false);
		}
		if (shape != null) {
			shape.setup(clicks);
			shape.croi.setName(getName());
			roi = shape.croi;
			fireROIChanged(roi);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (shape != null) {
			shape.dispose();
		}
		if (tempShape != null) {
			tempShape.dispose();
		}
	}
}
