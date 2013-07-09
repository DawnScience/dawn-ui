package org.dawnsci.plotting.api.filter;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;

public interface IPlottingFilter {

	/**
	 * You may override the 
	 * @param system
	 * @param trace
	 */
	public void filter(IPlottingSystem system, TraceWillPlotEvent trace);
	
	/**
	 * Replaces any filtered traces we have made with original data
	 * and leaves this filter active.
	 * 
	 * If this is the first filter in the chain, it will reset the data
	 * of the trace to original data. If not the data will be reset to 
	 * how it was before this filter was called - i.e. the originally passed
	 * in data to the filter.
	 * 
	 * If you are implementing IPlottingFilter, use the AbstractPlottingFilter
	 * version of reset.
	 */
	public void reset();
	
	/**
	 * The data rank to filter. Currently you must implement this as either 1 or 2. 1
	 * for XY plots and 2 for 2D data like images and surfaces.
	 * @return the rank of data to apply this filter to.
	 */
	public int getRank();
	
	/**
	 * 
	 * @return true if filter active.
	 */
	public boolean isActive();

	/**
	 * Sets whether the filter is live without undoing any previous filtering.
	 * 
	 * @param active
	 */
	public void setActive(boolean active);

}
