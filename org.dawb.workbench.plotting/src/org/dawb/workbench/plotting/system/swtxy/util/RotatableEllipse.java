package org.dawb.workbench.plotting.system.swtxy.util;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.swt.SWT;

/**
 * A Draw2D ellipse that allows its orientation to be set. Its location is the centre of rotation
 * (not the top-left corner of its bounding box)
 */
public class RotatableEllipse extends Shape {
	private static PrecisionPoint centre = new PrecisionPoint(0.5, 0.5);
	private AffineTransform affine; // transforms unit square (origin at top-left corner) to transformed rectangle
	private PointList box; // bounding box of ellipse
	private boolean outlineOnly = false;
	private boolean showMajorAxis = false;

	/**
	 * Unit circle centred on origin
	 */
	public RotatableEllipse() {
		affine = new AffineTransform();
	}

	/**
	 * 
	 * @param cx centre
	 * @param cy centre
	 * @param major axis length
	 * @param minor axis length
	 * @param angle of major axis from horizontal (in degrees, positive for anti-clockwise)
	 */
	public RotatableEllipse(double cx, double cy, double major, double minor, double angle) {
		affine = new AffineTransform();
		affine.setTranslation(cx - 0.5 * major, cy - 0.5 * minor);
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
	 * Set angle of rotated ellipse to given degrees (positive for anti-clockwise)
	 * @param degrees
	 */
	public void setAngle(double degrees) {
		Point oc = affine.getTransformed(centre);
		affine.setRotationDegrees(-degrees);
		Point nc = affine.getTransformed(centre);
		affine.setTranslation(affine.getTranslationX() + oc.preciseX() - nc.preciseX(), affine.getTranslationY() + oc.preciseY() - nc.preciseY());
		calcBox();
	}

	/**
	 * @return angle of rotation in degrees (positive for anti-clockwise)
	 */
	public double getAngleDegrees() {
		return -affine.getRotationDegrees();
	}

	/**
	 * @return centre of ellipse
	 */
	public Point getCentre() {
		return affine.getTransformed(centre);
	}

	/**
	 * @return major and minor axis lengths
	 */
	public double[] getAxes() {
		return new double[] { affine.getScaleX(), affine.getScaleY()};
	}

	/**
	 * Get point on ellipse at given angle
	 * @param degrees (positive for anti-clockwise)
	 * @return
	 */
	public Point getPoint(double degrees) {
		double angle = -Math.toRadians(degrees);
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		PrecisionPoint p = new PrecisionPoint(0.5*(c+1), 0.5*(s+1));
		return affine.getTransformed(p);
	}

	/**
	 * @param show if true, show major axis
	 */
	public void showMajorAxis(boolean show) {
		showMajorAxis = show;
	}

	/**
	 * @return affine transform (this is a copy)
	 */
	public AffineTransform getAffineTransform() {
		return affine.clone();
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
	public void setLocation(Point p) {
		affine.setTranslation(p.preciseX(), p.preciseY());
		calcBox();
	}

	@Override
	public boolean containsPoint(int x, int y) {
		if (outlineOnly) {
			double d = affine.getInverseTransformed(new PrecisionPoint(x, y)).getDistance(centre);
			return Math.abs(d - 0.5) < 2./Math.max(affine.getScaleX(), affine.getScaleY());
		}

		if (!super.containsPoint(x, y) || !box.polygonContainsPoint(x, y))
			return false;

		Point p = affine.getInverseTransformed(new PrecisionPoint(x, y));
		return p.getDistance(centre) <= 0.5;
	}

	@Override
	public void setFill(boolean b) {
		super.setFill(b);
		outlineOnly  = !b;
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
		double ax = affine.getScaleX();
		double ay = affine.getScaleY();
		graphics.drawOval(0, 0, (int) ax, (int) ay);
		if (showMajorAxis) {
			ay *= 0.5;
			graphics.drawLine(0, (int) ay, (int) ax, (int) ay);
		}
		graphics.popState();
	}

}