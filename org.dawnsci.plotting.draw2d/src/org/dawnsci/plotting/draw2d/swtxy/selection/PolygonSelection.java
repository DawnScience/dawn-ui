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

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;

public class PolygonSelection extends AbstractSelectionRegion {

	Polygon polygon;
	private static final Color magenta = new Color(null, 238, 0,	238);

	public PolygonSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(magenta);
		setAlpha(80);
		setLineWidth(2);
	}

	@Override
	public void setMobile(boolean mobile) {
		super.setMobile(mobile);
		if (polygon != null)
			polygon.setMobile(mobile);
	}

	@Override
	public void createContents(Figure parent) {
		polygon = new Polygon(parent, this);
		polygon.setCursor(Draw2DUtils.getRoiMoveCursor());

		parent.add(polygon);
		sync(getBean());
		setOpaque(false);
	}

	@Override
	public boolean containsPoint(int x, int y) {
		return polygon.containsPoint(x, y);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.POLYGON;
	}

	@Override
	protected void updateBounds() {
		if (polygon != null) {
			Rectangle b = polygon.updateFromHandles();
			polygon.setBounds(b);
		}
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		g.setLineStyle(SWT.LINE_DOT);
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.drawPolygon(clicks);
	}

	@Override
	public void initialize(PointList clicks) {
		if (polygon != null) {
			polygon.setup(clicks);
			fireROIChanged(createROI(true));
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-polygon.png";
	}

	@Override
	protected IROI createROI(boolean recordResult) {
		if (recordResult) {
			roi = polygon.croi;
		}
		return polygon.croi;
	}

	@Override
	protected void updateRegion() {
		if (polygon != null && roi instanceof PolylineROI) {
			polygon.updateFromROI((PolylineROI) roi);
			sync(getBean());
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 0; // signifies unlimited presses
	}

	@Override
	public void dispose() {
		super.dispose();
		if (polygon != null) {
			polygon.dispose();
		}
	}

	class Polygon extends PolylineROIShape {

		public Polygon(Figure parent, AbstractSelectionRegion region) {
			super(parent, region);
		}

		@Override
		public boolean containsPoint(int x, int y) {
			if (croi == null)
				return super.containsPoint(x, y);
			return getROI().containsPoint(cs.getPositionValue(x, y));
		}

		@Override
		public void updateFromROI(PolylineROI proi) {
			if (croi == null) {
				if (proi instanceof PolygonalROI) {
					croi = (PolygonalROI) proi;
				} else {
					croi = new PolygonalROI();
				}
			}

			super.updateFromROI(proi);
		}

		@Override
		protected PolylineROI createPolylineROI() {
			return new PolygonalROI();
		}

		@Override
		protected void fillShape(Graphics graphics) {
			graphics.fillPolygon(points);
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			Rectangle b = getParent().getBounds();
			Draw2DUtils.drawClippedPolyline(graphics, points, b, true);
		}
	}
}
