package org.dawnsci.plotting.api;

import org.dawnsci.plotting.api.filter.IFilterDecorator;
import org.dawnsci.plotting.api.tool.IToolPageSystem;

/**
 * This is a service for wrapping PlottingFactory for
 * those that prefer to use a service rather than a 
 * factory.
 * @author fcp94556
 *
 */
public interface IPlottingService {

	/**
	 * Use this method to create a new plotting system.
	 * This system may then be used to create plotting
	 * on an SWT component by calling createPlotPart(...)
	 * 
	 * @return
	 * @throws Exception
	 */
	public IPlottingSystem createPlottingSystem() throws Exception;

	/**
	 * Get a plotting system. (Same as getPlottingSystem(name, false))
	 * 
	 * @param plotName
	 * @return
	 */
	public IPlottingSystem getPlottingSystem(String plotName);
	
	/**
	 * Get a plotting system from the registered plotting system list.
	 * 
	 * @param plotName
	 * @param threadSafe - if true, the plotting system will be wrapped in a class which ensures all
	 * calls are thread safe.
	 * @return
	 */
	public IPlottingSystem getPlottingSystem(String plotName, boolean threadSafe);
	/**
	 * 
	 * Return a tool system for a given plot name
	 * (The actual implementation uses the getAdapter(IToolPageSystem.class)
	 * call on to IPlottingSystem.)
	 * 
	 * @param plotName
	 * @return
	 */
	public IToolPageSystem getToolSystem(String plotName);
	
	
	/**
	 * Register a plotting system. Usually there is no need to call this as AbstractPlottingSystem
	 * will register automatically.
	 * 
	 * @param plotName
	 * @param system
	 * @return
	 */
	public IPlottingSystem registerPlottingSystem(final String plotName, final IPlottingSystem system);

	/**
	 * 
	 * @param plotName
	 * @return
	 */
	public IPlottingSystem removePlottingSystem(String plotName);

	
	/**
	 * A plotting system may have a filter decorator registered with it which
	 * can interscept and modify data before it is plotted.
	 * 
	 * @param system
	 * @return
	 */
	public IFilterDecorator createFilterDecorator(IPlottingSystem system);

	
}
