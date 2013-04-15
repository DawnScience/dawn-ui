package org.dawnsci.plotting.api.trace;

public interface ITraceContainer {

	/**
	 * 
	 * @return the trace which 
	 */
	public ITrace getTrace();
	
	/**
	 * This method may do nothing if the trace cannot be changed.
	 * @param trace
	 */
	public void setTrace(ITrace trace);
}
