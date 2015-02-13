package org.dawnsci.plotting.tools.fitting;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;

/**
 * A runnable job with associated x & y data. The job is designed for function
 * fitting where x and y data define the ROI limits over which function fitting 
 * should be performed. 
 * @author wnm24546
 *
 */
public abstract class FittingJob extends Job {
	
	/**
	 * Executes this job with a specified name.
	 * @param name
	 */
	public FittingJob(String name) {
		super(name);
	}
	
	Dataset x;
	Dataset y;
	
	/**
	 * Set x & y data from separate datasets.
	 * @param x 
	 * @param y
	 */
	public void setData(Dataset x, Dataset y) {
		this.x = x.clone();
		this.y = y.clone();
	}
	
	/**
	 * Set x & y data from an array where index 0 is the x and 1 is the y data.
	 * @param limits
	 */
	public void setData(Dataset[] limits) {
		this.x = limits[0].clone();
		this.y = limits[1].clone();
	}
}
