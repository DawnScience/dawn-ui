package org.dawnsci.plotting.api.trace;

import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

public interface IWindowTrace extends ITrace {
	
	/**
	 * Sets a window of the data visible.
	 * @param roi
	 */
	public void setWindow(ROIBase roi);
}
