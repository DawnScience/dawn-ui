package org.dawnsci.plotting.api.trace;

import java.util.Collection;

/**
 * No methods in this interface are thread safe.
 * 
 * @author fcp94556
 *
 */
public interface ITraceSystem {
	
	
	/**
	 * Creates a line trace used for 1D plotting. This does not add the trace
	 * or give it any data.
	 * 
	 * @param traceName
	 * @return
	 */
	public IImageTrace createImageTrace(String traceName);

	/**
	 * Creates a line trace used for 1D plotting. This does not add the trace
	 * or give it any data.
	 * 
	 * @param traceName
	 * @return
	 */
	public ILineTrace createLineTrace(String traceName);
	
	
	/**
	 * Creates a surface trace used for 3D plotting. This does not add the trace
	 * or give it any data.
	 * 
	 * @param traceName
	 * @return
	 */
	public ISurfaceTrace createSurfaceTrace(String traceName);

	
	/**
	 * Creates a stack trace used for 3D plotting. This does not add the trace
	 * or give it any data.
	 * 
	 * A stack trace is a 3D set of lines.
	 * 
	 * @param traceName
	 * @return
	 */
	public ILineStackTrace createLineStackTrace(String traceName);

	/**
	 * Creates a stack trace used for 2D plotting. This does not add the trace
	 * or give it any data.
	 * 
	 * A stack trace is a 3D set of images. Only one image is visible at a time
	 * in the 2D plot.
	 * 
	 * @param traceName
	 * @return
	 */
	public IImageStackTrace createImageStackTrace(String traceName);

	/**
	 * Adds and plots the trace.
	 * @param trace
	 */
	public void addTrace(ITrace trace);

	/**
	 * Adds and plots the trace.
	 * @param trace
	 */
	public void removeTrace(ITrace trace);

    /**
     * return a plotted trace by name.
     * @param name
     * @return
     */
	public ITrace getTrace(String name);
	
	/**
	 * Call this method to retrieve what is currently plotted.
	 * See all ITraceListener.
	 * 
	 * @return
	 */
	public Collection<ITrace> getTraces();

	/**
	 * Call this method to retrieve what is currently plotted by trace type
	 * See all ITraceListener.
	 * 
	 * @return
	 */
	public Collection<ITrace> getTraces(Class<? extends ITrace> clazz);
	

	/**
	 * Add a listener to be notified of new traces plotted
	 * @param l
	 */
	public void addTraceListener(final ITraceListener l);
	
	/**
	 * Remove listener to avoid memory leaks
	 * @param l
	 */
	public void removeTraceListener(final ITraceListener l);
	
	/**
	 * Renames the trace, better than calling setName on the ITrace as the
	 * collection of traces is updated properly. No event will be fired.
	 */
	public void renameTrace(ITrace trace, String name) throws Exception;

	
}
