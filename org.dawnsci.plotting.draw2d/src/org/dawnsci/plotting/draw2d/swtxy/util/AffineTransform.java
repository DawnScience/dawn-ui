/*-
 * Copyright 2012 Diamond Light Source Ltd.
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

package org.dawnsci.plotting.draw2d.swtxy.util;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Represent an affine transformation
 * <pre>
 * (x') = ( ca  -sa ) ( sx   0 ) (x) + (tx)
 * (y')   ( sa   ca ) (  0  sy ) (y)   (ty)
 * </pre>
 * where sx, sy are scale factors, tx, ty are translations and
 * ca, sa are cosine and sine of rotation angle (anti-clockwise is positive)
 */
public class AffineTransform {
	private double scaleX = 1.0, scaleY = 1.0, dx, dy, cdy, cos = 1.0, sin;
	private double angle; // in radians (anti-clockwise is positive)
	private double ratio = 1.0; // factor for scaling y when aspect ratio is not unity

	@Override
	protected AffineTransform clone() {
		AffineTransform a = new AffineTransform();
		a.scaleX = scaleX;
		a.scaleY = scaleY;
		a.dx = dx;
		a.dy = dy;
		a.setRotation(angle);
		return a;
	}

	private void calcCachedDy() {
		cdy = dy + scaleY * (1 - ratio) * 0.5;
	}

	/**
	 * Sets the value for the amount of scaling to be done along both axes.
	 * 
	 * @param scale
	 *            Scale factor
	 */
	public void setScale(double scale) {
		scaleX = scaleY = scale;
		calcCachedDy();
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
		calcCachedDy();
	}

	/**
	 * Sets the rotation angle (positive is anti-clockwise).
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
	 * Sets the rotation angle (positive is anti-clockwise).
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
		calcCachedDy();
	}

	/**
	 * Sets the aspect ratio y/x for post-transform point scaling
	 * @param aspect
	 */
	public void setAspectRatio(double aspect) {
		ratio = aspect;
		calcCachedDy();
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

	/**
	 * @return rotation angle (positive is anti-clockwise).
	 */
	public double getRotation() {
		return angle;
	}

	/**
	 * @return rotation angle in degrees (positive is anti-clockwise).
	 */
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
		y = y * cos + x * sin;
		x = temp;
		if (p instanceof PrecisionPoint) {
			return new PrecisionPoint(x + dx, ratio * y + cdy);
		}
		return new Point((int) Math.round(x + dx), (int) Math.round(ratio * y + cdy));
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
		double y = (p.preciseY() - cdy)/ratio;
		double temp;

		temp = x * cos + y * sin;
		y = y * cos - x * sin;
		x = temp;
		x /= scaleX;
		y /= scaleY;
		if (p instanceof PrecisionPoint) {
			return new PrecisionPoint(x, y);
		}
		return new Point((int) Math.round(x), (int) Math.round(y));
	}

	/**
	 * Returns bounding box
	 * @return bounds
	 */
	public Rectangle getBounds() {
		Rectangle r = new Rectangle();
		Point p = getTransformed(new Point(0,0));
		r.setLocation(p);
		r.union(p);
		r.union(getTransformed(new Point(1,0)));
		r.union(getTransformed(new Point(1,1)));
		r.union(getTransformed(new Point(0,1)));
		return r;
	}
}
