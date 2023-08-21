package org.dawnsci.datavis.manipulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dawnsci.datavis.api.IXYData;
import org.dawnsci.datavis.api.utils.XYDataImpl;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceViewIterator;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.Comparisons.Monotonicity;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.StringDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;

import uk.ac.diamond.scisoft.analysis.dataset.function.Interpolation1D;

public class DataManipulationUtils {
	
	/**
	 * Take list of IXYData and return new list where data sets are cropped and interpolated
	 * onto a common x axis.
	 *<p>
	 * Optionally an x axis to act as the main axis (onto which all other are interpolated), can be
	 * passed. If it is, an empty int[] of length two can also be included, to return the indices
	 * the main axis needs to be cropped to to match the output list
	 * 
	 * @param data
	 * @param testX
	 * @param outIndices
	 * @return commonData
	 */
	public static List<IXYData> getCompatibleDatasets(List<IXYData> data, IDataset testX, int[] outIndices){
		return getCompatibleDatasets(data, testX, outIndices, false);
	}

	public static List<IXYData> getCompatibleDatasets(List<IXYData> data, IDataset testX, int[] outIndices, boolean linear){

		if (data.isEmpty()) {
			return data;
		}

		IDataset[] xall;
		int k;
		if (testX == null) {
			xall = new IDataset[data.size()];
			k = 0;
		} else {
			xall = new IDataset[data.size() + 1];
			xall[0] = testX;
			k = 1;
		}
		for (IXYData d : data) {
			xall[k++] = d.getX();
		}

		boolean dataAndNull = false;
		boolean needsChecks = false;

		Dataset test = DatasetUtils.convertToDataset(xall[0]);

		for (int i = 1; i < xall.length; i++) {
			if ((test == null) != (xall[i] == null)) dataAndNull = true;
			if (test != null && !test.equals(xall[i])) needsChecks = true;
		}

		//xdata and no xdata not supported
		if (dataAndNull) return null;

		IXYData first = data.get(0);
		if (test == null) {
			//TODO make sure yDatasets != null
			int size = first.getY().getSize();

			for (IXYData d : data) {
				if (d.getY().getSize() != size) return null;
			}

			return data;
		}

		if (!needsChecks) {
			if (outIndices != null) {
				outIndices[0] = 0;
				outIndices[1] = testX.getSize();
			}
			return data;
		}

		double[] commonValues = checkXaxisHasCommonRangeForInterpolation(xall);

		if (commonValues == null) return null;

		List<IXYData> output = new ArrayList<IXYData>();

		Monotonicity m = Comparisons.findMonotonicity(test);
		boolean up = m == Monotonicity.NONDECREASING || m == Monotonicity.STRICTLY_INCREASING;

		int maxpos;
		int minpos;
		if (up) {
			minpos = DatasetUtils.findIndexGreaterThan(test, commonValues[0]);
			maxpos = DatasetUtils.findIndexGreaterThan(test, commonValues[1]);
		} else {
			minpos = DatasetUtils.findIndexLessThan(test, commonValues[1]);
			maxpos = DatasetUtils.findIndexLessThan(test, commonValues[0]);
		}
		minpos = Math.max(minpos - 1, 0);
		maxpos = Math.min(maxpos, test.getSize());

		if (outIndices != null && outIndices.length == 2) {
			outIndices[0] = minpos;
			outIndices[1] = maxpos;
		}

		IDataset xnew =  test.getSlice(new int[] {minpos},new int[]{maxpos},null);
		xnew.setName(test.getName());
		
		int start = 0;
		
		if (testX == null) {
			IDataset y = first.getY().getSlice(new int[] {minpos},new int[]{maxpos},null);

			XYDataImpl d = new XYDataImpl(xnew, y, first.getLabel(), first.getFileName(), first.getDatasetName(), first.getLabelName(), new SliceND(y.getShape()));

			output.add(d);
			
			start = 1;
		}

		for (int i = start; i < data.size(); i++) {
			IXYData d = data.get(i);
			Dataset x = DatasetUtils.convertToDataset(d.getX());
			Dataset y = DatasetUtils.convertToDataset(d.getY());

			try {
				Dataset id = linear ? Maths.interpolate(x, y, xnew, null, null) : Interpolation1D.splineInterpolation(x, y, xnew);
				output.add(new XYDataImpl(xnew, id, d.getLabel(),
						d.getFileName(), d.getDatasetName(), d.getLabelName(), new SliceND(xnew.getShape())));
			} catch (Exception e) {
				throw new IllegalArgumentException(String.format("cannot interpolate values in %s:%s as \n%s",
						d.getFileName(), d.getDatasetName(), e.toString()), e);
			}
		}

		return output;
	}
	
	private static double[] checkXaxisHasCommonRangeForInterpolation(IDataset[] xaxis) {
		double infimum = Double.NEGATIVE_INFINITY; // greatest lower bound
		double supremum = Double.POSITIVE_INFINITY; // least upper bound

		for (IDataset x : xaxis) {
			infimum = Math.max(infimum, x.min().doubleValue());
			supremum = Math.min(supremum, x.max().doubleValue());
		}

		if (infimum > supremum) return null;

		return new double[] {infimum, supremum};
	}
	
	/**
	 * Combine a list of data into a stacked image
	 * 
	 * Inputs must have the same x-axis ({@link #getCompatibleDatasets} may be used to produce the input) 
	 * 
	 * @param list
	 * @return combinedData
	 */
	public static Dataset combine(List<IXYData> list) {
		if (list == null || list.isEmpty()) return null;

		IDataset x0 = list.get(0).getX();

		IDataset[] all = new IDataset[list.size()];
		Dataset names = DatasetFactory.zeros(StringDataset.class, all.length);
		Dataset labels = DatasetFactory.zeros(DoubleDataset.class, all.length);

		int count = 0;
		boolean anyNaNs = false;
		String lName = null;
		for (IXYData file : list) {

			names.set(new File(file.getFileName()).getName() + ":" + file.getDatasetName(), count);
			double l = file.getLabel();
			if (Double.isNaN(l)) {
				anyNaNs = true;
			} else if (lName == null) {
				lName = file.getLabelName();
			}
			labels.set(l, count);
			Dataset ds1 = DatasetUtils.convertToDataset(file.getY()).getSliceView().squeeze();
			ds1.setShape(1, ds1.getShapeRef()[0]);
			all[count++] = ds1;
		}

		Dataset conc = DatasetUtils.concatenate(all, 0);

		conc.setName("Combination");

		try {
			AxesMetadata md = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			md.setAxis(1, x0);
			md.addAxis(0, names);
			if (!anyNaNs) {
				labels.setName(lName);
				md.addAxis(0, labels);
			}
			conc.setMetadata(md);
		} catch (MetadataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return conc;
	}
	
	/**
	 * Get an iterator over a 2D stack of XY data.
	 * <p>
	 * Iterates over every 1D dataset (fastest axis) of a 2D dataset
	 * <p>
	 * If 2D dataset has StringDataset axis for slow dimension, this is used as the name of each returned dataset
	 * <p>
	 * Iterator may return null if slice fails
	 * 
	 * @param data
	 * @return
	 */
	public static Iterator<IDataset> getXYIterator(ILazyDataset data) {
		
		SliceViewIterator it = new SliceViewIterator(data, null, new int[] {1});

		return new Iterator<IDataset>() {

			@Override
			public IDataset next() {
				ILazyDataset next = it.next();
				Dataset ds = null;
				try {
					ds = DatasetUtils.sliceAndConvertLazyDataset(next);
				} catch (DatasetException e) {
					//no slice, so should be ok
					return null;
				}

				
				AxesMetadata xm = ds.getFirstMetadata(AxesMetadata.class);
				if (xm != null) {
					ILazyDataset[] axes = xm.getAxes();
					if (axes[0] != null && axes[0] instanceof StringDataset) {
						String name = ((StringDataset)axes[0]).get();
						ds.setName(name);
					}
				}
				
				return ds;
			}

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
		};
	}
}
