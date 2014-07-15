/*-
 * Copyright 2012 Diamond Light Source Ltd.
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
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.IPolylineROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;

public class PolylineSelection extends ROISelectionRegion<IPolylineROI> {

	public PolylineSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.cyan);
	}

	@Override
	protected ROIShape<IPolylineROI> createShape(Figure parent) {
		return new Polyline(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.POLYLINE;
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		g.setLineStyle(SWT.LINE_DOT);
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.drawPolyline(clicks);
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-polyline.png";
	}

	@Override
	public int getMaximumMousePresses() {
		return 0; // signifies unlimited presses
	}

	class Polyline extends PolylineROIShape<IPolylineROI> {

		public Polyline(Figure parent, AbstractSelectionRegion<IPolylineROI> region) {
			super(parent, region);
			setAreaTranslatable(true);
		}

		@Override
		protected IPolylineROI createNewROI() {
			return new PolylineROI();
		}

		private final static int TOLERANCE = 2;

		@Override
		public boolean containsPoint(int x, int y) {
			if (croi == null)
				return super.containsPoint(x, y);
			double[] pt = cs.getValueFromPosition(x, y);
			return croi.isNearOutline(pt[0], pt[1], TOLERANCE);
		}

		@Override
		protected void fillShape(Graphics graphics) {
			// do nothing
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			Rectangle b = getParent().getBounds();
			Draw2DUtils.drawClippedPolyline(graphics, points, b, false);
			if (isShowLabel()) {
				try {
					graphics.setForegroundColor(ColorConstants.black);
					graphics.setAlpha(255);
					graphics.drawString(getName(), points.getMidpoint());
				} catch (IndexOutOfBoundsException ignored) {
					// Ok no label then.
				}
			}
		}
	}
}
