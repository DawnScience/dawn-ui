package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.IDataset;

/**
 * Interface for modifying data before plotting in DataVis
 * 
 * Used for stacking, offsetting, normalising etc
 *
 */
public interface IPlotDataModifier {
	
	/**
	 * Configure the plot modifier using parameters from
	 * the plotting system, e.g. axis range
	 * 
	 * @param system
	 */
	public void configure(IPlottingSystem<?> system);

	/**
	 * Do the modification
	 * @param data
	 * @return modified
	 */
	public IDataset modifyForDisplay(IDataset data);
	
	/**
	 * Called between each update, so state can be reset
	 */
	public void init();
	
	/**
	 * Which rank data the modifier supports
	 * @param rank
	 * @return supported
	 */
	public boolean supportsRank(int rank);
	
	/**
	 * The name of the modifier
	 * @return name
	 */
	public String getName();
	
}
