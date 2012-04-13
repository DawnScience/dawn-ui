package org.dawb.workbench.plotting.system.swtxy.util;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;

/**
 * Represent an affine transformation
 * <pre>
 * (x') = ( ca  -sa ) ( sx   0 ) (x) + (tx)
 * (y')   ( sa   ca ) (  0  sy ) (y)   (ty)
 * </pre>
 * where sx, sy are scale factors, tx, ty are translations and
 * ca, sa are cosine and sine of rotation angle (clockwise is positive)
 */
public class AffineTransform {
	private double scaleX = 1.0, scaleY = 1.0, dx, dy, cos = 1.0, sin;
	private double angle; // in radians (clockwise is positive)
	private PointList box = new PointList(4);

	/**
	 * Sets the value for the amount of scaling to be done along both axes.
	 * 
	 * @param scale
	 *            Scale factor
	 */
	public void setScale(double scale) {
		scaleX = scaleY = scale;
	}

	/**
	 * Sets the value for the amount of scaling to be done along X and Y axes
	 * individually.
	 * 
	 * @param x
	 *            Amount of scaling on X axis
	 * @param y
	 *            Amount of scaling on Y axis
	 */
	public void setScale(double x, double y) {
		scaleX = x;
		scaleY = y;
	}

	/**
	 * Sets the rotation angle (positive is clockwise).
	 * 
	 * @param angle
	 *            Angle of rotation in radians
	 */
	public void setRotation(double angle) {
		this.angle = angle;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
	}

	/**
	 * Sets the rotation angle (positive is clockwise).
	 * 
	 * @param angle
	 *            Angle of rotation in degrees
	 */
	public void setRotationDegrees(double angle) {
		setRotation(Math.toRadians(angle)); 
	}

	/**
	 * Sets the translation amounts for both axes.
	 * 
	 * @param x
	 *            Amount of shift on X axis
	 * @param y
	 *            Amount of shift on Y axis
	 * @since 2.0
	 */
	public void setTranslation(double x, double y) {
		dx = x;
		dy = y;
	}

	public double getScaleX() {
		return scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public double getTranslationX() {
		return dx;
	}

	public double getTranslationY() {
		return dy;
	}

	public double getRotation() {
		return angle;
	}

	public double getRotationDegrees() {
		return Math.toDegrees(getRotation());
	}

	/**
	 * Returns a new transformed Point of the input Point based on the
	 * transformation values set.
	 * 
	 * @param p
	 *            Point being transformed
	 * @return The transformed Point or PrecisionPoint (if input was a PrecisionPoint)
	 */
	public Point getTransformed(Point p) {
		double x = p.preciseX() * scaleX;
		double y = p.preciseY() * scaleY;
		double temp = x * cos - y * sin;
		y = x * sin + y * cos;
		x = temp;
		if (p instanceof PrecisionPoint) {
			return new PrecisionPoint(x + dx, y + dy);
		}
		return new Point((int) Math.round(x + dx), (int) Math.round(y + dy));
	}

	/**
	 * Returns a new inverse-transformed Point of the input Point based on the
	 * transformation values set.
	 * 
	 * @param p
	 *            Point being inverse-transformed
	 * @return The inverse-transformed Point or PrecisionPoint (if input was a PrecisionPoint)
	 */
	public Point getInverseTransformed(Point p) {
		double x = p.preciseX() - dx;
		double y = p.preciseY() - dy;
		double temp;

		temp = x * cos + y * sin;
		y = - x * sin + y * cos;
		x = temp;
		x /= scaleX;
		y /= scaleY;
		if (p instanceof PrecisionPoint) {
			return new PrecisionPoint(x, y);
		}
		return new Point((int) Math.round(x), (int) Math.round(y));
	}

	/**
	 * Returns a point list of transformed unit square
	 * @return transformed unit square
	 */
	public PointList getTransformedUnitSquare() {
		box.removeAllPoints();
		box.addPoint(getTransformed(new Point(0,0)));
		box.addPoint(getTransformed(new Point(1,0)));
		box.addPoint(getTransformed(new Point(1,1)));
		box.addPoint(getTransformed(new Point(0,1)));
		return box;
	}
}
