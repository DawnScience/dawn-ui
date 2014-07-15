/*-
 * Copyright 2014 Diamond Light Source Ltd.
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

import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.region.ILockableRegion;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.roi.IParametricROI;

abstract class LockableSelectionRegion<T extends IParametricROI> extends ROISelectionRegion<T> implements ILockableRegion {

	LockableSelectionRegion(String name, ICoordinateSystem coords) {
		super(name, coords);
		labelColour = ColorConstants.black;
		if (labelFont != null)
			labelFont.dispose();
		labelFont = new Font(Display.getCurrent(), "Dialog", 10, SWT.BOLD);
	}

	@Override
	abstract protected ParametricROIShape<T> createShape(Figure parent);

	private ParametricROIShape<T> tempShape = null;

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		if (clicks.size() <= 1)
			return;

		g.setLineStyle(SWT.LINE_DOT);
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		if (tempShape == null) {
			tempShape = createShape(null);
		}
		tempShape.setup(clicks, false);
		
		tempShape.outlineShape(g, parentBounds);
	}

	@Override
	public int getMaximumMousePresses() {
		return 2;
	}

	@Override
	public void initialize(PointList clicks) {
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
		if (tempShape != null) {
			tempShape.dispose();
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
		((ParametricROIShape<?>) shape).setCentreMobile(isCenterMovable);
	}

	@Override
	public boolean isOuterMovable() {
		return isOuterMovable;
	}

	@Override
	public void setOuterMovable(boolean isOuterMovable) {
		this.isOuterMovable = isOuterMovable;
		((ParametricROIShape<?>) shape).setOuterMobile(isOuterMovable);
	}
}
