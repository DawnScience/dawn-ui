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

import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.diamond.scisoft.analysis.roi.IROI;

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
