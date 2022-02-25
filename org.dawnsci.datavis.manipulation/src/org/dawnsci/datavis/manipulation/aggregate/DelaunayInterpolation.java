package org.dawnsci.datavis.manipulation.aggregate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.SliceND;

import il.ac.idc.jdt.DelaunayTriangulation;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.Triangle;

/**
 * Use a Delaunay triangulation to linearly interpolate from a scattered set of
 * samples
 */
public class DelaunayInterpolation {

	/**
	 * Interpolate set of 1D datasets located at given x, y coordinates in new sample grid
	 * @param ox original x
	 * @param oy original y
	 * @param ov original values at given x, y coordinates (must be all the same shape)
	 * @param sx new x to be used for grid sampling
	 * @param sy new y to be used for grid sampling
	 * @return linearly interpolated values in grid
	 */
	public static DoubleDataset gridInterpolate(Dataset ox, Dataset oy, List<Dataset> ov, Dataset sx, Dataset sy) {
		int max = ov.size();
		if (max < 3) {
			throw new IllegalArgumentException("Three or more datasets requireds");
		}
		if (ox.getSize() != max || oy.getSize() != max) {
			throw new IllegalArgumentException("Number of coordinates must match number of datasets");
		}

		int[] oShape = ov.get(0).getShapeRef();
		List<Point> points = new ArrayList<>();
		for (int n = 0; n < max; n++) { // use Z as index
			if (n > 0 && !Arrays.equals(oShape, ov.get(n).getShapeRef())) {
				throw new IllegalArgumentException("Value datasets must all be same shape");
			}
			points.add(new Point(ox.getDouble(n), oy.getDouble(n), n));
		}
		DelaunayTriangulation dt = new DelaunayTriangulation(points);

		int xSize = sx.getSize();
		int ySize = sy.getSize();
		int aSize = ov.get(0).getSize(); // axis
		int oRank = oShape.length;
		int[] aShape = Arrays.copyOf(oShape, oRank + 2);
		aShape[oRank]= ySize;
		aShape[oRank + 1]= xSize;
		DoubleDataset agg = DatasetFactory.zeros(aShape);
		SliceND s = new SliceND(agg.getShapeRef());
		DoubleDataset tmp = DatasetFactory.zeros(aSize);

		Point pt = new Point();
		for (int j = 0; j < ySize; j++) {
			double pty = sy.getDouble(j);
			pt.setY(pty);
			s.setSlice(oRank, j, j+1, 1);

			for (int i = 0; i < xSize; i++) {
				double px = sx.getDouble(i);
				pt.setX(px);

				// find triangle where new sample lies and
				// calculate barycentric coordinates for interpolation
				Triangle t = dt.find(pt);
				Point va = t.getA();
				Point vb = t.getB();
				Point vc = t.getC();

				double a =  va.getX();
				px -= a;
				double bx = vb.getX() - a;
				double cx = vc.getX() - a;
				a =  va.getY();
				double by = vb.getY() - a;
				double cy = vc.getY() - a;
				double py = pty - a;

				// solve p = f_b b + f_c c
				double det = bx * cy - by * cx;
				double fb = (px*cy - py*cx)/det;
				double fc = (-px*by + py*bx)/det;
				
				baryCentricInterpolate(tmp, ov.get((int) va.getZ()), ov.get((int) vb.getZ()), ov.get((int) vc.getZ()), fb, fc);

				s.setSlice(oRank + 1, i, i+1, 1);
				agg.setSlice(tmp, s);
			}
		}

		return agg;
	}

	private static void baryCentricInterpolate(DoubleDataset zp, Dataset za, Dataset zb, Dataset zc, double fb, double fc) {
		int n = zp.getSize();
		for (int i = 0; i < n; i++) {
			double z = za.getDouble(i);
			zp.setAbs(i, z + fb*(zb.getDouble(i) - z) + fc*(zc.getDouble(i) - z));
		}
	}
}
