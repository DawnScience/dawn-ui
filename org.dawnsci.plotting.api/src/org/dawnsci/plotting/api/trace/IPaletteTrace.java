package org.dawnsci.plotting.api.trace;

import org.dawnsci.plotting.api.histogram.HistogramBound;
import org.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.swt.graphics.PaletteData;

public interface IPaletteTrace extends IAxesTrace {

	
	/**
	 * PaletteData for creating the image/surface from the AbstractDataset
	 * @return
	 */
	public PaletteData getPaletteData();
	
	/**
	 * Setting palette data causes the image to redraw with the new palette.
	 * @param paletteData
	 */
	public void setPaletteData(PaletteData paletteData);
	
	/**
	 * Returns the last image service bean sent to the service for getting
	 * the image.
	 * 
	 * @return
	 */
	public ImageServiceBean getImageServiceBean();
	
	/**
	 * Call to add a palette listener
	 * @param pl
	 */
	public void addPaletteListener(IPaletteListener pl);
	
	
	/**
	 * Call to remove a palette listener
	 * @param pl
	 */
	public void removePaletteListener(IPaletteListener pl);

	/**
	 * The min intensity for generating the image
	 * @return
	 */
	public Number getMin();

	/**
	 * The max intensity for generating the image
	 * @return
	 */
	public Number getMax();

	/**
	 * Gets the Nan cut
	 * @return
	 */
	public HistogramBound getNanBound();

	/**
	 * Gets the min cut, a RGB and a bound.
	 * @return
	 */
	public HistogramBound getMinCut();

	/**
	 * Gets the max cut, a RGB and a bound.
	 * @return
	 */
	public HistogramBound getMaxCut();

	/**
	 * Sets the Nan cut
	 * @return
	 */
	public void setNanBound(HistogramBound bound);

	/**
	 * Gets the min cut, a RGB and a bound.
	 * @return
	 */
	public void setMinCut(HistogramBound bound);

	/**
	 * Gets the min cut, a RGB and a bound.
	 * @return
	 */
	public void setMaxCut(HistogramBound bound);

	/**
	 * The min intensity for generating the image/surface trace
	 * @return
	 */
	public void setMin(Number min);
	
	/**
	 * The max intensity for generating the image/surface trace
	 * @return
	 */
	public void setMax(Number max);

	/**
	 * Set wheter an update to the image trace triggers a rehistogram
	 * @param rescaleHistogram
	 */
	public void setRescaleHistogram(boolean rescaleHistogram);

	/**
	 * Is the imagetrace set to rescale the histogram when a new image update occurs
	 * @return the current state.
	 */
	public boolean isRescaleHistogram();
}
