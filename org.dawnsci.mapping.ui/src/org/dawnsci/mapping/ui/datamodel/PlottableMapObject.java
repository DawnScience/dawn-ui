package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.january.dataset.IDataset;

/**
 * Interface describing all objects that can be plotted in the mapping
 * perspective.
 *
 */
public interface PlottableMapObject extends MapObject {

	/**
	 * Gets the (unique) long name for the object
	 * 
	 * @return longName
	 */
	public String getLongName();
	
	/**
	 * Gets the plottable data
	 * 
	 * @return map
	 */
	public IDataset getMap();
	
	/**
	 * Returns whether this data is from a live scan
	 * 
	 * @return
	 */
	public boolean isLive();
	
	/**
	 * Call to update the internal data from file.
	 * <p>
	 * For live files when the map size is increasing
	 * 
	 */
	public void update();
	
	/**
	 * Get the transparency value for the map
	 * 
	 * @return
	 */
	public int getTransparency();
	
	/**
	 * Set the transparency value for the map
	 * 
	 * @return
	 */
	public void setTransparency(int transparency);
	
	/**
	 * Set the color range for the map
	 * 
	 * @param range
	 */
	public void setColorRange(double[] range);

	/**
	 * Get the color range
	 * 
	 * @return range
	 */
	public double[] getColorRange();

	/**
	 * Get the slice of the parent data that corresponds to the specified x,y
	 * position
	 * 
	 * @param x
	 * @param y
	 * @return data
	 */
	public IDataset getSpectrum(double x, double y);
	
	/**
	 * Get the path to the file
	 * 
	 * @return
	 */
	public String getPath();
	
	/**
	 * Return true if the object is plotted
	 * 
	 * @return
	 */
	public boolean isPlotted();
	
	/**
	 * Set whether the object should be plotted or not
	 * 
	 * @param plot
	 */
	public void setPlotted(boolean plot);
}
