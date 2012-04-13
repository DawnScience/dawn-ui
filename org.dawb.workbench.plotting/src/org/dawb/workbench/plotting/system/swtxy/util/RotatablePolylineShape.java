package org.dawb.workbench.plotting.system.swtxy.util;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;

/**
 * A Draw2D polyline shape that allows its orientation to be set
 */
class RotatablePolylineShape extends RotatablePolygonShape {
	private int tolerance = 2;


	public RotatablePolylineShape(PointList list, double angle) {
		super(list, angle);
	}

	@Override
	protected void fillShape(Graphics graphics) {
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.drawPolyline(npl);
//		graphics.pushState();
//		graphics.setAdvanced(true);
//		graphics.rotate(angle);
//		graphics.drawPolyline(opl);
//		graphics.popState();
	}

	@Override
	protected boolean shapeContainsPoint(int x, int y) {
		return npl.polylineContainsPoint(x, y, tolerance);
	}

	/**
	 * Set tolerance of hit detection of shape
	 * @param tolerance (number of pixels between point and segment)
	 */
	public void setTolerance(int tolerance) {
		this.tolerance = tolerance;
	}
}
