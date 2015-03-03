package org.dawnsci.plotting.histogram;

import org.dawnsci.plotting.histogram.ui.HistogramWidget;
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
	 * This will be the larger of the min or max of the image values
	 *
	 * @return double max value
	 */
	public double getMax();

	/**
	 * Return the minimum value for the histogram.
	 * This will be the smaller of the min or max of the image values.
	 *
	 * @return double min value
	 */
	public double getMin();

	/**
	 * Sets the maximum value for the histogram
	 *
	 * @param double max value
	 */
	public void setMax(double max);

	/**
	 * Sets the minimum value for the histogram
	 *
	 * @param double min value
	 */
	public void setMin(double min);

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

	public void inputChanged(HistogramWidget histogramWidget,
			Object currentInput, Object object);

	public void dispose();

}
