package org.dawb.workbench.plotting.system.swtxy.util;

import org.eclipse.draw2d.AbstractPointListShape;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A Draw2D polygon shape that allows its orientation to be set
 */
class RotatablePolygonShape extends AbstractPointListShape {
	protected float angle;
	protected final PointList opl; // original (unrotated) points
	protected final PointList npl; // reference to rotated points
	private final AffineTransform rot;

	public RotatablePolygonShape(PointList list, double angle) {
		super();
		opl = new PointList(list.size());
		npl = getPoints();
		rot = new AffineTransform();
		npl.removeAllPoints();
		npl.addAll(list);
		opl.addAll(list);
		setAngle(angle);
	}

	@Override
	public void addPoint(Point pt) {
		opl.addPoint(pt);
		super.addPoint(pt);
		recalcPoints();
	}

	@Override
	public void removeAllPoints() {
		opl.removeAllPoints();
		super.removeAllPoints();
	}

	@Override
	public void insertPoint(Point pt, int index) {
		opl.insertPoint(pt, index);
		super.insertPoint(rot.getTransformed(pt), index);
	}

	@Override
	public void removePoint(int index) {
		opl.removePoint(index);
		super.removePoint(index);
	}

	@Override
	public void setPoint(Point pt, int index) {
		opl.setPoint(pt, index);
		super.setPoint(rot.getTransformed(pt), index);
	}

	@Override
	public void setPoints(PointList points) {
		opl.removeAllPoints();
		opl.addAll(points);
		super.setPoints(points);
		recalcPoints();
	}
	
	/**
	 * Set angle of rotated polyline to given degrees clockwise
	 * @param degrees
	 */
	public void setAngle(double degrees) {
		angle = (float) degrees;
		rot.setRotationDegrees(degrees);
		recalcPoints();
	}

	private void recalcPoints() {
		final int n = npl.size();
		for (int i = 0; i < n; i++) {
			npl.setPoint(rot.getTransformed(opl.getPoint(i)), i);
		}
		Rectangle b = npl.getBounds();
		setBounds(b);
	}

	@Override
	protected void fillShape(Graphics graphics) {
		graphics.fillPolygon(npl);
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.drawPolygon(npl);
//		graphics.pushState();
//		graphics.setAdvanced(true);
//		graphics.rotate(angle);
//		graphics.drawPolygon(opl);
//		graphics.popState();
	}

	@Override
	protected boolean shapeContainsPoint(int x, int y) {
		return npl.polygonContainsPoint(x, y);
	}
}
