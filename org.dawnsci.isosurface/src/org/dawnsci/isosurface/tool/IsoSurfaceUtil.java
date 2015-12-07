package org.dawnsci.isosurface.tool;

import org.dawnsci.isosurface.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;


/**
 * 
 * Simple class to hold some broad functions for gathering data on the a isosurface
 * @author Joel Ogden
 * 
 *
 */
public final class IsoSurfaceUtil {
	
	/**
	 * 
	 * @param lz - The lazy dataSet to use
	 * @return A pretty inaccurate estimation of the min and max isovalue of the histogram
	 * 
	 */
	public static double[] estimateMinMaxIsoValueFromDataSet(ILazyDataset lz)
	{
		double min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		
		// test first third
		IDataset slice = lz.getSlice(
				new int[] { lz.getShape()[0]/3, 0,0}, 
				new int[] {1+lz.getShape()[0]/3, lz.getShape()[1], lz.getShape()[2]},
				new int[] {1,1,1});
		
		double[] minMaxStats = getStatsFromSlice(slice);
		if (min >= minMaxStats[0])
		{
			min = minMaxStats[0];
		}
		if (max <= minMaxStats[1])
		{
			max = minMaxStats[1];
		}
		
		// test center
		slice = lz.getSlice(
				new int[] { lz.getShape()[0]/2, 0,0}, 
				new int[] {1+lz.getShape()[0]/2, lz.getShape()[1], lz.getShape()[2]},
				new int[] {1,1,1});
		
		minMaxStats = getStatsFromSlice(slice);
		if (min >= minMaxStats[0])
		{
			min = minMaxStats[0];
		}
		if (max <= minMaxStats[1])
		{
			max = minMaxStats[1];
		}
		
		// test second third
		slice = lz.getSlice(
				new int[] {2 * lz.getShape()[0]/3, 0,0}, 
				new int[] {1+lz.getShape()[0]/3, lz.getShape()[1], lz.getShape()[2]},
				new int[] {1,1,1});
		
		minMaxStats = getStatsFromSlice(slice);
		if (min >= minMaxStats[0])
		{
			min = minMaxStats[0];
		}
		if (max <= minMaxStats[1])
		{
			max = minMaxStats[1];
		}
		
		
		return new double[] {min, max};
	}
	
	/**
	 * 
	 * @param slice - The dataSet
	 * @return The min max stats of the slice
	 * @see org.eclipse.dawnsci.plotting.api.histogram.IImageService.getFastStatistics(ImageServiceBean bean)

	 */
	public static double[] getStatsFromSlice(IDataset slice)
	{
		final IImageService service = (IImageService)Activator.getService(IImageService.class);
		return service.getFastStatistics(new ImageServiceBean((Dataset)slice, HistoType.MEAN));
	}
	
	
}
