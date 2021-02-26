package org.dawnsci.datavis.api;

/**
 * Service to delegate opening of "Non data" files
 *
 */
public interface IScriptFileOpener {
	
	/**
	 * Determine if the service should open the file
	 * 
	 * @param path
	 * @return canOpen
	 */
	boolean canOpen(String path);
	
	/**
	 * Instruct service to open the file
	 * 
	 * @param path
	 */
	void open(String path);

}
