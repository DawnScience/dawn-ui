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

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.diamond.scisoft.analysis.roi.IFitROI;

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
