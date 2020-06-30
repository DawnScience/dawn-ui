package org.dawnsci.datavis.model;

import org.dawnsci.datavis.api.IPlotMode;

/**
 * Main interface for interacting with the plot in the DataVis perspective
 * <p>
 * The IPlotController listens to changes of the state of the IFileController,
 * and updates the plot accordingly
 *
 */
public interface IPlotController {

	/**
	 * Initialisation method, should be called by OSGI
	 */
	void init();
	
	/**
	 * Get the current plotting mode the controller is using
	 * 
	 * @return mode
	 */
	IPlotMode getCurrentMode();

	/**
	 * Switch plotting mode, using the DataOptions as the master dataset
	 * 
	 * @param mode
	 * @param dataOptions
	 */
	void switchPlotMode(IPlotMode mode, DataOptions dOptions);

	/**
	 * Get the plot modes supported for a certain dataset rank
	 * 
	 * @param rank
	 * @return modes
	 */
	public IPlotMode[] getPlotModes(int rank);
	
	/**
	 * Get the plot modes supported for a certain DataOptions
	 * 
	 * @param rank
	 * @return modes
	 */
	public IPlotMode[] getPlotModes(DataOptions dOptions);
	
	/**
	 * Force the plot controller to re-sample the FileController state
	 * and replot the data
	 */
	void forceReplot();
	
	/**
	 * Dispose of inner resources
	 */
	public void dispose();
	
	/**
	 * Get the currently applicable plot modifiers
	 * 
	 * @return modifiers
	 */
	public IPlotDataModifier[] getCurrentPlotModifiers();
	
	/**
	 * Enable this plot modifier
	 * @param modifier
	 */
	public void enablePlotModifier(IPlotDataModifier modifier);
	
	/**
	 * Get the enabled modifier
	 * @return
	 */
	public IPlotDataModifier getEnabledPlotModifier();
	
	/**
	 * Set a new color provider
	 * <p>
	 * Color providers are used to change the line plot color scheme
	 * 
	 * @param colorProvider
	 */
	void setColorProvider(ITraceColourProvider colorProvider);

	/**
	 * Get the current color provider
	 * @return
	 */
	ITraceColourProvider getColorProvider();
	
	/**
	 * Add a listener to be notified when the plot mode changes
	 * 
	 * @param l
	 */
	void addPlotModeListener(PlotModeChangeEventListener l);
	
	/**
	 * Remove the plot mode listener
	 * 
	 * @param l
	 */
	void removePlotModeListener(PlotModeChangeEventListener l);
	
	/**
	 * Determine if co-slicing is enabled
	 * <p>
	 * If the plot mode support multiple traces, and co-slicing is enabled,
	 * datasets of the same size will all have slices changed when one changes
	 * 
	 * @return
	 */
	boolean isCoSlicingEnabled();

	/**
	 * Enable or Disable co-slicing
	 * 
	 * @param coSlicingEnabled
	 */
	void setCoSlicingEnabled(boolean coSlicingEnabled);

	/**
	 * Force a replot when only the slicing of data has changed
	 * 
	 * @param dop
	 */
	void replotOnSlice(DataOptions dop);
}
