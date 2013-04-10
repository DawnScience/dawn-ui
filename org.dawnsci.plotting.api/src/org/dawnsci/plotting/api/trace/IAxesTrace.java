package org.dawnsci.plotting.api.trace;

import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public interface IAxesTrace extends ITrace {

	
	/**
	 * The set of axes, either a list of size 2 for a 2D trace or a list of size 3 for a 3D one.
	 * May contain nulls (z is often null for intensity).
	 * @return
	 */
	public List<IDataset> getAxes();
	
	
	/**
	 * 
	 * @return true if plot is currently plotting.
	 */
	public boolean isActive();

	
	/**
	 * Labels for the axes. The data set name of the axis used if not set.
	 * Should be size 3 but may have nulls.
	 * @return
	 */
	public List<String> getAxesNames();

}
