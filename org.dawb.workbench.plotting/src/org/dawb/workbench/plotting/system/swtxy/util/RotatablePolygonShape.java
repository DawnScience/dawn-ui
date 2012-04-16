package org.dawb.workbench.plotting.system.swtxy.util;

import org.eclipse.draw2d.AbstractPointListShape;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A Draw2D polygon shape that allows its orientation to be set. Its location is the centre of
 * rotation (not the top-left corner of its bounding box)
 */
public class RotatablePolygonShape extends AbstractPointListShape {
	protected final PointList opl; // original (unrotated) points
	protected final PointList npl; // reference to rotated points
	private final AffineTransform affine;

	private RotatablePolygonShape(int size) {
		opl = new PointList(size);
		npl = getPoints();
		npl.setSize(size);
		affine = new AffineTransform();
	}

	/**
	 * Generic constructor
	 * @param list
	 * @param angle in degrees
	 */
	public RotatablePolygonShape(PointList list, double angle) {
		this(list.size());
		npl.removeAllPoints();
		npl.addAll(list);
		opl.addAll(list);
		setAngle(angle);
	}

	/**
	 * Constructor for rectangle
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param angle in degrees
	 */
	public RotatablePolygonShape(int x, int y, int width, int height, double angle) {
		this(4);
		opl.addPoint(0, 0);
		opl.addPoint(width, 0);
		opl.addPoint(width, height);
		opl.addPoint(0,   height);
		npl.addAll(opl);
		affine.setTranslation(x, y);
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
		super.insertPoint(affine.getTransformed(pt), index);
	}

	@Override
	public void removePoint(int index) {
		opl.removePoint(index);
		super.removePoint(index);
	}

	@Override
	public void setPoint(Point pt, int index) {
		opl.setPoint(pt, index);
		super.setPoint(affine.getTransformed(pt), index);
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
		affine.setRotationDegrees(degrees);
		recalcPoints();
	}

	/**
	 * @return angle of rotation in degrees
	 */
	public double getAngleDegrees() {
		return affine.getRotationDegrees();
	}

	@Override
	public void setLocation(Point p) {
		affine.setTranslation(p.preciseX(), p.preciseY());
		recalcPoints();
	}

	private void recalcPoints() {
		final int n = npl.size();
		for (int i = 0; i < n; i++) {
			npl.setPoint(affine.getTransformed(opl.getPoint(i)), i);
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
