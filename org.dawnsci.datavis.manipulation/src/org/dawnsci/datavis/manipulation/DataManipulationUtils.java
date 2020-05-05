package org.dawnsci.datavis.manipulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dawnsci.datavis.api.IXYData;
import org.dawnsci.datavis.api.utils.XYDataImpl;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceViewIterator;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
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

		IDataset[] xall = new IDataset[data.size()];

		for (int i = 0; i < data.size(); i++) {
			xall[i] = data.get(i).getX();
		}

		boolean dataAndNull = false;
		boolean needsChecks = false;

		IDataset test = testX != null ? testX : xall[0];
		
		if (testX != null) {
			IDataset[] xallextra = new IDataset[data.size()+1];
			xallextra[0] = test;
			System.arraycopy(xall, 0, xallextra, 1, xall.length);
			xall = xallextra;
		}
		

		for (int i = 1; i < xall.length; i++) {
			if ((test == null) != (xall[i] == null)) dataAndNull = true;
			if (test != null && !test.equals(xall[i])) needsChecks = true;
		}

		//xdata and no xdata not supported
		if (dataAndNull) return null;

		if (test == null) {
			//TODO make sure yDatasets != null
			int size = data.get(0).getY().getSize();

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

		int maxpos = ROISliceUtils.findPositionOfClosestValueInAxis(test, commonValues[1])-1;
		int minpos = ROISliceUtils.findPositionOfClosestValueInAxis(test, commonValues[0])+1;
		
		if (outIndices != null && outIndices.length == 2) {
			outIndices[0] = minpos;
			outIndices[1] = maxpos;
		}

		IDataset xnew =  test.getSlice(new int[] {minpos},new int[]{maxpos},null);
		xnew.setName(test.getName());
		
		int start = 0;
		
		if (testX == null) {
			IDataset y = data.get(0).getY().getSlice(new int[] {minpos},new int[]{maxpos},null);

			XYDataImpl d = new XYDataImpl(xnew, y, data.get(0).getFileName(), data.get(0).getDatasetName(), new SliceND(y.getShape()));

			output.add(d);
			
			start = 1;
		}

		for (int i = start; i < data.size(); i++) {

			IDataset x = data.get(i).getX();
			IDataset y1 = data.get(i).getY();

			output.add(new XYDataImpl(xnew, Interpolation1D.splineInterpolation(x, y1, xnew),data.get(i).getFileName(),data.get(i).getDatasetName(), new SliceND(xnew.getShape())));
		}

		return output;
	}
	
	private static double[] checkXaxisHasCommonRangeForInterpolation(IDataset[] xaxis) {
		double min = Double.NEGATIVE_INFINITY;
		double max = Double.POSITIVE_INFINITY;

		for (IDataset x : xaxis) {
			min = Math.max(min, x.min().doubleValue());
			max = Math.min(max, x.max().doubleValue());
		}

		if (min > max) return null;

		return new double[] {min, max};
	}
	
	/**
	 * Combine a list of data into a stacked image
	 * 
	 * Inputs must have the same x-axis ({@link #getCompatibleDatasets} may be used to produce the input) 
	 * 
	 * @param list
	 * @return combinedData
	 */
	public static IDataset combine(List<IXYData> list) {
		if (list == null || list.isEmpty()) return null;

		IDataset x0 = list.get(0).getX();

		IDataset[] all = new IDataset[list.size()];
		IDataset names = DatasetFactory.zeros(StringDataset.class, new int[] {list.size()});

		int count = 0;
		for (IXYData file : list) {

			names.set(new File(file.getFileName()).getName() + ":" + file.getDatasetName(), count);
			IDataset ds1 = file.getY().getSliceView().squeeze();
			ds1.setShape(new int[]{1,ds1.getShape()[0]});
			all[count++] = ds1;

		}

		Dataset conc = DatasetUtils.concatenate(all, 0);

		conc.setName("Combination");

		try {
			AxesMetadata md = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			md.setAxis(1, x0);
			md.addAxis(0, names);
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
