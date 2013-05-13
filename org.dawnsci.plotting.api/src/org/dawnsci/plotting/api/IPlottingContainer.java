package org.dawnsci.plotting.api;


/**
 * Any editor, view or control can implement this interface to allow access to its
 * embedded plotting system 
 */
public interface IPlottingContainer {
	/**
	 * The plotting system embedded in the container. May be return null
	 * @return plotting system
	 */
	public IPlottingSystem getPlottingSystem();
}
