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
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.CircularFitROI;
import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.IPolylineROI;
import uk.ac.diamond.scisoft.analysis.roi.handler.ParametricROIHandler;

public class CircleFitSelection extends AbstractSelectionRegion<CircularFitROI> {
	private final static Logger logger = LoggerFactory.getLogger(CircleFitSelection.class);

	private static final int MIN_POINTS = 3; // minimum number of points to define ellipse
	FRShape shape;

	public CircleFitSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.yellow);
		setAlpha(80);
		setLineWidth(2);
		labelColour = ColorConstants.black;
	}

	@Override
	public void setMobile(boolean mobile) {
		super.setMobile(mobile);
		if (shape != null)
			shape.setMobile(mobile);
	}

	@Override
	public void createContents(Figure parent) {
		shape = new FRShape(parent, this);
		shape.setCursor(Draw2DUtils.getRoiMoveCursor());

		parent.add(shape);
		sync(getBean());
		shape.setLineWidth(getLineWidth());
	}

	@Override
	public boolean containsPoint(int x, int y) {
		return shape.containsPoint(x, y);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.CIRCLEFIT;
	}

	@Override
	protected void updateBounds() {
		if (shape != null) {
			Rectangle b = shape.updateFromHandles();
			if (b != null)
				shape.setBounds(b);
		}
	}

	private FRShape tempShape = null;

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.setLineStyle(Graphics.LINE_DOT);
		g.drawPolyline(clicks);
		if (clicks.size() >= MIN_POINTS) {
			if (tempShape == null) {
				tempShape = new FRShape();
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
	public void initialize(PointList clicks) {
		if (tempShape != null) {
			tempShape.setVisible(false);
		}
		if (shape != null) {
			shape.setup(clicks);
			roi = shape.croi;
			fireROIChanged(getROI());
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-circle.png";
	}

	@Override
	protected CircularFitROI createROI(boolean recordResult) {
		if (recordResult) {
			roi = shape.croi;
		}
		return shape.croi;
	}

	private void updateLabel(CircularROI croi) {
		setLabel(String.format("%.2fpx", croi.getRadius()));
	}

	@Override
	protected void updateRegion() {
		if (shape != null && roi instanceof CircularFitROI) {
			shape.updateFromROI((CircularFitROI) roi);
			sync(getBean());
			updateLabel((CircularROI) roi);
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 0;
	}

	@Override
	public int getMinimumMousePresses() {
		return MIN_POINTS;
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
				Rectangle r = new Rectangle(getPoint(0.75 * Math.PI), getCentre());
				graphics.drawLine(r.getTopLeft(), r.getBottomRight());
				graphics.setAlpha(192);
				graphics.setBackgroundColor(ColorConstants.white);
				graphics.setForegroundColor(labelColour);
				graphics.setFont(labelFont);
				graphics.fillString(label, r.getCenter().getTranslated(0, -labeldim.height));
			}
		}

		@Override
		public String toString() {
			double rad = cs.getPositionFromValue(getROI().getRadius())[0];
			return "CirSel: cen=" + getCentre() + ", rad=" + rad;
		}
	}
}
