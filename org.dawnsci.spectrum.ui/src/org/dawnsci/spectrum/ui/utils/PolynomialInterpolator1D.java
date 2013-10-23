package org.dawnsci.spectrum.ui.utils;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;

public class PolynomialInterpolator1D {

	public static IDataset interpolate(IDataset oldx, IDataset oldy, IDataset newx) {
		
		//TODO more sanity checks on inputs
		
		DoubleDataset dx = (DoubleDataset)DatasetUtils.cast((AbstractDataset)oldx,AbstractDataset.FLOAT64);
		DoubleDataset dy = (DoubleDataset)DatasetUtils.cast((AbstractDataset)oldy,AbstractDataset.FLOAT64);
		
		boolean sorted = true;
		double maxtest = oldx.getDouble(0);
		for (int i = 1; i < ((AbstractDataset)oldx).count(); i++) {
			if (maxtest > oldx.getDouble(i))  {
				sorted = false;
				break;
			}
			maxtest = dx.getDouble(i);
		}
		
		double[] sortedx = null;
		double[] sortedy = null;
		
		if (!sorted) {
			IntegerDataset sIdx = getIndiciesOfSorted(dx);
			sortedx = new double[dx.getData().length];
			sortedy = new double[dy.getData().length];
			
			for (int i = 0 ; i < sIdx.getSize(); i++) {
				sortedx[i] = dx.getDouble(sIdx.get(i));
				sortedy[i] = dy.getDouble(sIdx.get(i));
			}
		} else {
			sortedx = dx.getData();
			sortedy = dy.getData();
		}
		
		SplineInterpolator si = new SplineInterpolator();
		PolynomialSplineFunction poly = si.interpolate(sortedx,sortedy);
		
		IDataset newy = newx.clone();
		newy.setName(oldy.getName()+"_interpolated");
		
		for (int i = 0; i < ((AbstractDataset)newx).count(); i++) {
			newy.set(poly.value(newx.getDouble(i)),i);
		}
		
		return newy;
	}
	
	public static IntegerDataset getIndiciesOfSorted(final IDataset d) {

		Integer[] dBox = new Integer[d.getSize()];

		for (int i = 0; i < dBox.length; i++) dBox[i] = i;

		Arrays.sort(dBox, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				double dif = d.getDouble(o1)-d.getDouble(o2);
				return dif > 0 ? (int)Math.ceil(dif) : (int)Math.floor(dif);
			}
		});

		int[] dint = new int[d.getSize()];
		for (int i = 0; i < dBox.length; i++) dint[i] = dBox[i];

		return new IntegerDataset(dint, new int[]{dint.length});
	}
	
	public static int[] getCommonRangeIndicies(IDataset x1, IDataset x2) {
		//TODO checks for no overlap etc
		double max1 = x1.max().doubleValue();
		double min1 = x1.min().doubleValue();
		
		double max2 = x2.max().doubleValue();
		double min2 = x2.min().doubleValue();
		
		double max = max1 < max2 ? max1 : max2;
		double min = min1 > min2 ? min1 : min2;
		//TODO max needs to be 1 lower, min needs to be 1 higher
		int maxpos = ROISliceUtils.findPositionOfClosestValueInAxis(x1, max);
		int minpos = ROISliceUtils.findPositionOfClosestValueInAxis(x1, min);
		
		return new int[]{minpos+1, maxpos-1};
	}
}
