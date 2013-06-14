package org.dawnsci.plotting.api;


/**
 * Any editor, view or control can implement this interface to allow access to its
 * embedded plotting system.
 * 
 * We should replace this with 
 * <code>IPlottingSystem system = (IPlottingSystem)part.getAdpater(IPlottingSystem.class)</code> 
 * instead but this requires the getAdpater(...) method to be implemented in several places.
 */
public interface IPlottingContainer {
	/**
	 * The plotting system embedded in the container. May be return null
	 * @return plotting system
	 */
	public IPlottingSystem getPlottingSystem();
}
