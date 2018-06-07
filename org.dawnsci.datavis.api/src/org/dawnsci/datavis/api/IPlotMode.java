package org.dawnsci.datavis.api;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

/**
 * Interface for mode of plotting in DataVis
 *
 */
public interface IPlotMode {

	/**
	 * Get the options describing the axes of the plot
	 * <p>
	 * i.e. "X", "Y", "Z", "Row", "Column"
	 * 
	 * @return options
	 */
	public String[] getOptions();
	
	/**
	 * Slice the data for the plot
	 * <p>
	 * Take the lazy dataset and return an array of IDataset
	 * with axis information set
	 * <p>
	 * This is expensive, there should be no UI thread calls in here
	 * 
	 * @param lz
	 * @param slice
	 * @param options
	 * @param system
	 * @return datasets
	 * @throws Exception
	 */
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice,Object[] options,IPlottingSystem<?> system) throws Exception;
	
	/**
	 * Takes the sliced IDatasets and displays them.
	 * <p>
	 * Occurs in the UI thread
	 * 
	 * @param data
	 * @param update
	 * @param system
	 * @param userObject
	 * @throws Exception
	 */
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception;
	
	/**
	 * Get the name of the plot mode
	 * 
	 * @return name
	 */
	public String getName();
	
	/**
	 * Whether this mode supports multiple traces as the same time
	 * 
	 * @return multiple
	 */
	public boolean supportsMultiple();
	
	/**
	 * Get minimum rank of dataset supported for this mode
	 * 
	 * @return rank
	 */
	public int getMinimumRank();
	
	/**
	 * Return whether this trace type is from this mode
	 * 
	 * @param trace
	 * @return
	 */
	public boolean isThisMode(ITrace trace);
	
	/**
	 * Get the dimensions of the data that will be plotted,
	 * as opposed to sliced, with the supplied option set
	 * 
	 * @param currentOptions
	 * @return dataDimensions
	 */
	public int[] getDataDimensions(Object[] currentOptions);

	
}
