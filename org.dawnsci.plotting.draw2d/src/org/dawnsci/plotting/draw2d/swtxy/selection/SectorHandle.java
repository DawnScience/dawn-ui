package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.draw2d.swtxy.util.AffineTransform;
import org.dawnsci.plotting.draw2d.swtxy.util.RotatableEllipse;
import org.dawnsci.plotting.draw2d.swtxy.util.RotatablePolygonShape;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.swt.graphics.Color;

/**
 * Elliptical sector handle
 */
public class SectorHandle extends SelectionHandle {

	private AffineTransform affine;
	/**
	 * 
	 * @param xAxis
	 * @param yAxis
	 * @param colour
	 * @param parent ellipse
	 * @param side (positive is outside ellipse)
	 * @param start angle in degrees
	 * @param end angle in degrees
	 */
	public SectorHandle(ICoordinateSystem coords, Color colour, RotatableEllipse parent, int side, double start, double end) {
		super(coords, colour, parent, side, start, end);
	}

	static private final int ARC_POINTS = 5; // number of points on arc

	/**
	 * Get point on ellipse at given angle
	 * @param degrees
	 * @return
	 */
	public Point getPoint(double degrees) {
		double angle = Math.toRadians(degrees);
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		PrecisionPoint p = new PrecisionPoint(0.5*(c+1), 0.5*(s+1));
		return affine.getTransformed(p);
	}

	@Override
	public Shape createHandleShape(Figure parent, int side, double[] params) {
		if (!(parent instanceof RotatableEllipse)) {
			throw new IllegalArgumentException("Parent shape of sector handle should be an ellipse");
		}
		RotatableEllipse ell = (RotatableEllipse) parent;
		PointList list = new PointList(2*ARC_POINTS);
		affine = ell.getAffineTransform();
		location = new PrecisionPoint(affine.getTranslationX(), affine.getTranslationY());

		final double sx = affine.getScaleX();
		final double sy = affine.getScaleY();
		double dr;
		if (side < 0) {
			dr = Math.min(sx, sy) + side; // difference in semi-axes
			if (dr < 1) {
				dr = 1;
			} else {
				dr = Math.floor(dr);
			}
		} else {
			dr = side;
		}

		double sa = params[0]; // start angle

		// generate point list for arcs
		double da = (params[1] - sa)/(ARC_POINTS-1);
		double a = sa;

		for (int i = 0; i <= ARC_POINTS; i++) {
			list.addPoint(getPoint(a));
			a += da;
		}
		affine.setScale(sx - dr, sy - dr);
		for (int i = 0; i <= ARC_POINTS; i++) {
			a -= da;
			list.addPoint(getPoint(a));
		}

		return new RotatablePolygonShape(list, 0);
	}
}
