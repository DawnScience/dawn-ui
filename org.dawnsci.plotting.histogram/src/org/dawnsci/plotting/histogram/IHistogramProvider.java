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
	 * Return the maximum for the histogram range
	 * 
	 * @return double maximum range
	 */
	public double getMaximumRange();

	/**
	 * Return the minimum range for the histogram
	 * 
	 * @return double minimum range
	 */
	public double getMininumRange();

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

	/**
	 * Return the dataset of x values for the histogram
	 * 
	 * @return dataset of x values
	 */
	public IDataset getXDataset();

	/**
	 * Return the dataset of y values for the histogram
	 * 
	 * @return dataset of y values
	 */
	public IDataset getYDataset();

}
