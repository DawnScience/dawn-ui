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
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class PolygonSelection extends ROISelectionRegion<PolygonalROI> {

	private static final Color magenta = new Color(null, 238, 0,	238);

	public PolygonSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(magenta);
	}

	protected ROIShape<PolygonalROI> createShape(Figure parent) {
		return new Polygon(parent, this);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.POLYGON;
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
	protected String getCursorPath() {
		return "icons/Cursor-polygon.png";
	}

	@Override
	protected PolygonalROI convertROI(IROI oroi) {
		if (oroi instanceof IPolylineROI) {
			return new PolygonalROI((IPolylineROI) oroi);
		}
		return super.convertROI(oroi);
	}

	@Override
	public int getMaximumMousePresses() {
		return 0; // signifies unlimited presses
	}

	class Polygon extends PolylineROIShape<PolygonalROI> {

		public Polygon(Figure parent, AbstractSelectionRegion<PolygonalROI> region) {
			super(parent, region);
			setAreaTranslatable(true);
		}

		@Override
		public boolean containsPoint(int x, int y) {
			if (croi == null)
				return super.containsPoint(x, y);
			return croi.containsPoint(cs.getValueFromPosition(x, y));
		}

		@Override
		protected PolygonalROI createNewROI() {
			return new PolygonalROI();
		}

		@Override
		protected void fillShape(Graphics graphics) {
			graphics.fillPolygon(points);
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			
			graphics.pushState();
			Rectangle b = getParent().getBounds();
			Draw2DUtils.drawClippedPolyline(graphics, points, b, true);
			
			if (isShowLabel()) {
				try {
					graphics.setForegroundColor(ColorConstants.black);
					graphics.setAlpha(255);
					graphics.drawString(getName(), points.getMidpoint());
				} catch (IndexOutOfBoundsException ignored) {
					// Ok no label then.
				}
			}
			graphics.popState();
		}
	}
}
