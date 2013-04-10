package org.dawnsci.plotting.api.trace;


import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * A representation of a plotted data set.
 * 
 * 
 * 
 * @author fcp94556
 *
 */
public interface ITrace {
	
	
	/**
	 * Name of trace, matches name of the abstract data set that originally created it.
	 * @return
	 */
	public String getName();
	/**
	 * Name of trace, matches name of the abstract data set that originally created it.
	 * @return
	 */
	public void setName(String name);
	
	
	/**
	 * Call this method to return a plotted data set by this trace.
	 */
	public IDataset getData();
	
	/**
	 * True if visible
	 * @return
	 */
	public boolean isVisible();

	/**
	 * True if visible
	 * @return
	 */
	public void setVisible(boolean isVisible);
	
	/**
	 * True if user trace (normally is)
	 * @return
	 */
	public boolean isUserTrace();

	/**
	 * True if visible
	 * @return
	 */
	public void setUserTrace(boolean isUserTrace);

	/**
	 * An object which may be set by API users to record information
	 * about the plot. Ideally avoid objects containing large data
	 * in this method.
	 * 
	 * @return
	 */
	public Object getUserObject();
	
	/**
	 * An object which may be set by API users to record information
	 * about the plot. Ideally avoid objects containing large data
	 * in this method.
	 * 
	 * @return
	 */
	public void setUserObject(Object userObject);
	
	/**
	 * @return true if trace is plotted using a 3D viewer.
	 * @return
	 */
	public boolean is3DTrace();

	/**
	 * Called to release system resources.
	 */
	public void dispose();
}
