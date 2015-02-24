package org.dawnsci.plotting.histogram;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

/**
 * This interface provides all the information to build up a histogram, such as
 * for use with a HistogramWidget
 *
 */
public interface IHistogramProvider {

	/**
	 * Return the number of bins for this histogram
	 * 
	 * @return number of bins
	 */
	public int getNumberOfBins();

	/**
	 * Return the maximum value for the histogram
	 * 
	 * @return double max value
	 */
	public double getMax();

	/**
	 * Return the minimum value for the histogram
	 * 
	 * @return double min value
	 */
	public double getMin();

	public IHistogramDatasets getDatasets();
	
	public interface IHistogramDatasets {
		/**
		 * Return the dataset of x values for the histogram
		 * 
		 * @return dataset of x values
		 */
		public IDataset getX();

		/**
		 * Return the dataset of y values for the histogram
		 * 
		 * @return dataset of y values
		 */
		public IDataset getY();

		/**
		 * Return the dataset of R values for the histogram
		 * 
		 * @return dataset of R values
		 */
		public IDataset getR();

		/**
		 * Return the dataset of G values for the histogram
		 * 
		 * @return dataset of G values
		 */
		public IDataset getG();

		/**
		 * Return the dataset of B values for the histogram
		 * 
		 * @return dataset of B values
		 */
		public IDataset getB();

		/**
		 * Return the dataset of RGBX values for the histogram
		 * 
		 * @return dataset of RGBx values
		 */
		public IDataset getRGBX();

	}

}
