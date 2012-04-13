package org.dawb.workbench.plotting.system.swtxy.util;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * A Draw2D ellipse that allows its orientation to be set
 */
class RotatableEllipse extends Shape {
	private static PrecisionPoint centre = new PrecisionPoint(0.5, 0.5);
	private AffineTransform affine; // transforms unit square (origin at top-left corner) to transformed rectangle
	private PointList box; // bounding box of ellipse

	/**
	 * 
	 * @param cx centre
	 * @param cy centre
	 * @param major axis length
	 * @param minor axis length
	 * @param angle of major axis from horizontal (in degrees, positive for clockwise)
	 */
	public RotatableEllipse(double cx, double cy, double major, double minor, double angle) {
		affine = new AffineTransform();
		affine.setTranslation(cx-0.5*major, cy-0.5*minor);
		affine.setScale(major, minor);
		setAngle(angle);
	}

	/**
	 * Set centre position
	 * @param cx
	 * @param cy
	 */
	public void setCentre(double cx, double cy) {
		Point oc = affine.getTransformed(centre);
		affine.setTranslation(affine.getTranslationX() + cx - oc.preciseX(), affine.getTranslationY() + cy - oc.preciseY());
		calcBox();
	}

	/**
	 * Set angle of rotated ellipse to given degrees (positive for clockwise)
	 * @param degrees
	 */
	public void setAngle(double degrees) {
		Point oc = affine.getTransformed(centre);
		affine.setRotationDegrees(degrees);
		Point nc = affine.getTransformed(centre);
		affine.setTranslation(affine.getTranslationX() + oc.preciseX() - nc.preciseX(), affine.getTranslationY() + oc.preciseY() - nc.preciseY());
		calcBox();
	}

	/**
	 * Set major and minor axes lengths
	 * @param major
	 * @param minor
	 */
	public void setAxes(double major, double minor) {
		Point oc = affine.getTransformed(centre);
		affine.setScale(major, minor);
		Point nc = affine.getTransformed(centre);
		affine.setTranslation(affine.getTranslationX() + oc.preciseX() - nc.preciseX(), affine.getTranslationY() + oc.preciseY() - nc.preciseY());
		calcBox();
	}

	private void calcBox() {
		box = affine.getTransformedUnitSquare();
		setBounds(box.getBounds());
	}

	@Override
	public boolean containsPoint(int x, int y) {
		if (!super.containsPoint(x, y) || !box.polygonContainsPoint(x, y))
			return false;

		Point p = affine.getInverseTransformed(new PrecisionPoint(x, y));
		return p.getDistance(centre) <= 0.5;
	}

	@Override
	protected void fillShape(Graphics graphics) {
		graphics.pushState();
		graphics.setAdvanced(true);
		graphics.setAntialias(SWT.ON);
		graphics.translate((int) affine.getTranslationX(), (int) affine.getTranslationY());
		graphics.rotate((float) affine.getRotationDegrees());
		// NB do not use Graphics#scale and unit shape as there are precision problems
		graphics.fillOval(0, 0, (int) affine.getScaleX(), (int) affine.getScaleY());
		graphics.popState();
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.pushState();
		graphics.setAdvanced(true);
		graphics.setAntialias(SWT.ON);
		graphics.translate((int) affine.getTranslationX(), (int) affine.getTranslationY());
		graphics.rotate((float) affine.getRotationDegrees());
		// NB do not use Graphics#scale and unit shape as there are precision problems
		Rectangle b = new Rectangle();
		b.union(new Point((int) affine.getScaleX(), (int) affine.getScaleY()));
		graphics.drawArc(b, 0, 360);
		graphics.popState();
	}

}