package org.dawnsci.plotting.api.trace;

public interface IDownsampledTrace extends ITrace {

	/**
	 * Provides the square bin used for the downsample
	 * @return
	 */
	public int getBin();
	
	/**
	 * Add listener to be notifed if the dawnsampling changes.
	 * @param l
	 */
	public void addDownsampleListener(IDownSampleListener l);
	
	/**
	 * Remove listener so that it is not notified.
	 * @param l
	 */
	public void removeDownsampleListener(IDownSampleListener l);
	

}
