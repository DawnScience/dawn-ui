package org.dawnsci.plotting.draw2d.swtxy.util;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class Draw2DUtils {

	/**
	 * Attempts to get the centre of a figure using its bounds.
	 * @param bx
	 * @return
	 */
	public static Point getCenter(Figure bx) {
		final Point   location = bx.getLocation();
		final Rectangle bounds = bx.getBounds();
		return new Point(location.x+(bounds.width/2), location.y+(bounds.height/2));
	}

	public static Cursor getRoiControlPointCursor() {
		return Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND);
	}
	public static Cursor getRoiMoveCursor() {
		return Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEALL);
	}


	/**
	 * Call to make SWT Image data for SWT Image using swing BufferedImage
	 * 
	 * This allows one to use Java 2d to draw the primitive.
	 * 
	 * @param bufferedImage
	 * @return
	 */
	public static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage
					.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(),
					colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0],
							pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage
					.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
						blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		return null;
	}

	/**
	 * Generate a curve from a parametrised point function
	 * @param fn
	 * @param lower
	 * @param upper
	 * @param delta
	 * @param minDistance
	 * @param maxAngle (in radians)
	 * @return a point list of curve
	 */
	public static PointList generateCurve(PointFunction fn, double lower, double upper, double delta, double minDistance, double maxAngle) {
		double vd = delta;
		PointList list = new PointList();
		Point pp, pc;
		pp = fn.calculatePoint(lower);
		list.addPoint(pp);
		Dimension dp, dc;
		dp = null;
		double ds = minDistance*minDistance;
		double cos = Math.cos(maxAngle);
		cos *= cos;
		boolean force = false;
		double vc = lower + vd;
		while (vc <= upper) {
			pc = fn.calculatePoint(vc);
			dc = pc.getDifference(pp);
			double xc = dc.preciseWidth();
			double yc = dc.preciseHeight();
			double sc = xc * xc + yc * yc;
			if (sc >= ds) {
				if (dp != null) {
					double xp = dp.preciseWidth();
					double yp = dp.preciseHeight();
					double sp = xp * xp + yp * yp;
					double cc = xc * xp + yc * yp;
					if (cc * cc < sc * sp *cos) { // angle too wide
						vd *= 0.5;
						vc = vc - vd;
						force = true; // prevent bouncing
						continue;
					}
				}
			} else if (!force) { // point too close
				vd *= 2;
				vc = vc + vd;
				continue;
			}
			list.addPoint(pc);
			pp = pc;
			dp = dc;
			force = false;
		}
		return list;
	}

	/**
	 * Draw a polyline clipped by given bounds
	 * @param g
	 * @param points of polyline
	 * @param bounds
	 * @param isPolygon if true, join last point to first
	 */
	public static void drawClippedPolyline(Graphics g, PointList points, Rectangle bounds, boolean isPolygon) {
		final int pts = points.size();
		if (pts < 2)
			return;

		double xl = bounds.preciseX();
		double xh = xl + bounds.preciseWidth();
		double yl = bounds.preciseY();
		double yh = yl + bounds.preciseHeight();

		Point p0;
		Point p1 = points.getPoint(0);
		PointList list = new PointList();
		int i = 1;
		double[] t = new double[2];

		boolean first = true;
		for (; i < pts || (isPolygon && i == pts); i++) {
			p0 = p1;
			p1 = points.getPoint(i % pts);

			t[0] = 0;
			t[1] = 1;
			double x0 = p0.preciseX();
			double y0 = p0.preciseY();
			double x1 = p1.preciseX();
			double y1 = p1.preciseY();
			double dx = x1 - x0;
			double dy = y1 - y0;

			if (dx == 0 && dy == 0)
				continue; // ignore null segment

			if (first) { // find first segment that is (partly) in bounds
				if (clip(xl - x0, dx, t) && clip(x0 - xh, -dx, t)
						&& clip(yl - y0, dy, t) && clip(y0 - yh, -dy, t)) {
					if (t[0] > 0) {
						p0 = new PrecisionPoint(Math.round(x0 + t[0] * dx), Math.round(y0 + t[0] * dy));
					}
					list.removeAllPoints();
					list.addPoint(p0);
					if (t[1] < 1) {
						PrecisionPoint p = new PrecisionPoint(Math.round(x0 + t[1] * dx), Math.round(y0 + t[1] * dy));
						list.addPoint(p);
						g.drawPolygon(list);
					} else {
						first = false;
						list.addPoint(p1);
					}
				}
			} else { // given that p0 is in bounds
				if (clip2(xl - x0, dx, t) && clip2(x0 - xh, -dx, t) && clip2(yl - y0, dy, t) && clip2(y0 - yh, -dy, t)) {
					if (t[1] < 1) {
						first = true;
						PrecisionPoint p = new PrecisionPoint(Math.round(x0 + t[1] * dx), Math.round(y0 + t[1] * dy));
						list.addPoint(p);
						g.drawPolyline(list);
					} else {
						list.addPoint(p1);
					}
				}
			}
		}

		if (list.size() > 0) {
			g.drawPolyline(list);
		}
	}

	private static boolean clip(double n, double d, double[] t) {
		if (d == 0) {
			return n <= 0;
		}

		double v = n/d;
		if (d > 0) {
			if (v > t[1])
				return false;
			if (v > t[0])
				t[0] = v;
		} else {
			if (v < t[0])
				return false;
			if (v < t[1])
				t[1] = v;
		}

		return true;
	}

	private static boolean clip2(double n, double d, double[] t) {
		if (d == 0) {
			return n <= 0;
		}

		double v = n/d;
		if (d > 0) {
			if (v > t[1])
				return false;
		} else {
			if (v < t[1])
				t[1] = v;
		}

		return true;
	}

}
