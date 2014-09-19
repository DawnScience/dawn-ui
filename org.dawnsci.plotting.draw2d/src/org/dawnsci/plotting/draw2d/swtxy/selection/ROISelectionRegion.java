/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

abstract class ROISelectionRegion<T extends IROI> extends AbstractSelectionRegion<T> {

	ROIShape<T> shape;

	ROISelectionRegion(String name, ICoordinateSystem coords) {
		super(name, coords);
		setAlpha(80);
		setLineWidth(2);
	}

	abstract protected ROIShape<T> createShape(Figure parent);

	@Override
	public void createContents(Figure parent) {
		shape = createShape(parent);

		parent.add(shape);
		sync(getBean());
		shape.setLineWidth(getLineWidth());
		if (roi != null)
			shape.updateFromROI(roi);
	}

	@Override
	public boolean containsPoint(int x, int y) {
		return shape.containsPoint(x, y);
	}

	@Override
	protected void updateBounds() {
		if (shape != null) {
			shape.updateFromHandles();
			Rectangle b = shape.getBounds();
			if (b != null)
				shape.setBounds(b);
		}
	}

	@Override
	protected T createROI(boolean recordResult) {
		if (recordResult) {
			roi = shape.croi;
		}
		return shape.croi;
	}

	@Override
	protected void updateRegion() {
		if (shape != null && roi != null) {
			shape.updateFromROI(roi);
			sync(getBean());
		}
	}

	@Override
	public void initialize(PointList clicks) {
		if (shape != null) {
			shape.setup(clicks);
			shape.croi.setName(getName());
			fireROIChanged(getROI());
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (shape != null) {
			shape.dispose();
		}
	}
}
