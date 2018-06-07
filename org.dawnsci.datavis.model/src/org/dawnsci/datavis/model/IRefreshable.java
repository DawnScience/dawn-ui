package org.dawnsci.datavis.model;

/**
 * Interface to describe something loaded into the datavis
 * perspective which might need to be refreshed, e.g. a 
 * file the is still being written to
 *
 */
public interface IRefreshable {

	/**
	 * Trigger the refresh
	 */
	public void refresh();
	
	/**
	 * Request the file be locally reloaded,
	 * instead of using the remote service
	 */
	public void locallyReload();
	
	/**
	 * Determine if the file is
	 * still growing and using the 
	 * remote service
	 * @return live
	 */
	public boolean isLive();
	
	/**
	 * Determine if the file contains any readable
	 * datasets
	 * 
	 * @return empty
	 */
	public boolean isEmpty();
	
	/**
	 * Return if the file has been initialised
	 * by an external initaliser
	 * 
	 * Initialisers can be used to set the
	 * default datasets to select and how they are plotted
	 * 
	 * @return initialised
	 */
	public boolean isInitialised();
	
	/**
	 * Set the flag to say the file has been initialised
	 */
	public void setInitialised();
	
}
