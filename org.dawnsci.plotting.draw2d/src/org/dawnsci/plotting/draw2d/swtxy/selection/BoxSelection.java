/*-
 * Copyright 2013 Diamond Light Source Ltd.
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
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.handler.ROIHandler;
import uk.ac.diamond.scisoft.analysis.roi.handler.RectangularROIHandler;

class BoxSelection extends AbstractSelectionRegion {

	Box box;

	BoxSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(RegionType.BOX.getDefaultColor());
		setAlpha(80);
		setLineWidth(2);
	}

	@Override
	public void setMobile(boolean mobile) {
		super.setMobile(mobile);
		if (box != null)
			box.setMobile(mobile);
	}

	@Override
	public void createContents(Figure parent) {
		box = new Box(parent, this);
		box.setCursor(Draw2DUtils.getRoiMoveCursor());

		parent.add(box);
		sync(getBean());
		setOpaque(false);
	}

	@Override
	public boolean containsPoint(int x, int y) {
		return box.containsPoint(x, y);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.BOX;
	}

	@Override
	protected void updateBounds() {
		if (box != null) {
			Rectangle b = box.updateFromHandles();
			if (b != null)
				box.setBounds(b);
		}
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		if (clicks.size() <= 1)
			return;

		g.setLineStyle(SWT.LINE_DOT);
		final Rectangle bounds = new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint());
		g.drawRectangle(bounds);
		g.setBackgroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.fillRectangle(bounds);
	}

	@Override
	public void initialize(PointList clicks) {
		if (box != null) {
			box.setup(clicks);
			fireROIChanged(getROI());
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-box.png";
	}

	@Override
	protected IROI createROI(boolean recordResult) {
		if (recordResult) {
			roi = box.croi;
		}
		return box.croi;
	}

	@Override
	protected void updateRegion() {
		if (box != null && roi instanceof RectangularROI) {
			box.updateFromROI((RectangularROI) roi);
			sync(getBean());
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 2;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (box != null) {
			box.dispose();
		}
	}

	protected void drawRectangle(Graphics g) {
		box.internalOutline(g);
	}

	protected void fillRectangle(Graphics g) {
		box.internalFill(g);
	}

	@Override
	public IROI getROI() {
		return box != null ? box.getROI() : super.getROI();
	}

	class Box extends ROIShape<RectangularROI> {

		public Box(Figure parent, AbstractSelectionRegion region) {
			super(parent, region);
		}

		@Override
		protected ROIHandler createROIHandler(IROI roi) {
			return new RectangularROIHandler((RectangularROI) roi);
		}

		@Override
		public void setCentre(Point nc) {
			double[] pt = cs.getPositionValue(nc.x(), nc.y());
			double[] pc = croi.getMidPoint();
			pt[0] -= pc[0];
			pt[1] -= pc[1];
			croi.addPoint(pt);
			dirty = true;
			calcBox(croi, true);
		}

		@Override
		public void setup(PointList corners) {
			final Point pa = corners.getFirstPoint();
			final Point pc = corners.getLastPoint();

			double[] a = cs.getPositionValue(pa.x(), pa.y());
			double[] c = cs.getPositionValue(pc.x(), pc.y());
			double ox = Math.min(a[0], c[0]);
			double oy = Math.min(a[1], c[1]);
			double lx = Math.abs(a[0] - c[0]);
			double ly = Math.abs(a[1] - c[1]);
			double angle = 0;
			if (lx < ly) {
				angle = 0.5*Math.PI;
				ox += lx;
				double t = lx;
				lx = ly;
				ly = t;
			}
			croi = new RectangularROI(ox, oy, lx, ly, angle);

			roiHandler.setROI(createROI(true));
			configureHandles();
		}

		@Override
		public String toString() {
			if (croi == null)
				return "BoxSel: undefined";

			int[] pt = cs.getValuePosition(croi.getPointRef());
			Point start = new Point(pt[0], pt[1]);
			int[] pta = cs.getValuePosition(0, 0);
			int[] ptb = cs.getValuePosition(croi.getLengths());

			return "BoxSel: start=" + start + ", major=" + (ptb[0] - pta[0]) + ", minor=" + (ptb[1] - pta[1]) + ", ang=" + croi.getAngleDegrees();
		}

		protected PointList generatePointList() {
			PointList pl = new PointList(4);
			int[] pt;
			RectangularROI proi = getROI();
			pt = cs.getValuePosition(proi.getPointRef());
			pl.addPoint(pt[0], pt[1]);
			pt = cs.getValuePosition(proi.getPoint(1, 0));
			pl.addPoint(pt[0], pt[1]);
			pt = cs.getValuePosition(proi.getPoint(1, 1));
			pl.addPoint(pt[0], pt[1]);
			pt = cs.getValuePosition(proi.getPoint(0, 1));
			pl.addPoint(pt[0], pt[1]);

			return pl;
		}

		private void internalFill(Graphics graphics) {
			graphics.pushState();
			graphics.setAdvanced(true);
			graphics.setAntialias(SWT.ON);
			graphics.fillPolygon(generatePointList());
			graphics.popState();
		}

		private void internalOutline(Graphics graphics) {
			graphics.pushState();
			graphics.setAdvanced(true);
			graphics.setAntialias(SWT.ON);

			Rectangle bnds = parent.getBounds();
			Draw2DUtils.drawClippedPolyline(graphics, generatePointList(), bnds, true);
			graphics.popState();
		}

		@Override
		protected void outlineShape(Graphics g) {
			drawRectangle(g);
		}

		@Override
		protected void fillShape(Graphics g) {
			fillRectangle(g);
		}
	}
}
