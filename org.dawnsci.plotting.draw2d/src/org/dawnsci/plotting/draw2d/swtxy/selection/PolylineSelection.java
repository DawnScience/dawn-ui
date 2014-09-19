/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.eclipse.dawnsci.analysis.api.roi.IPolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

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
