package org.dawnsci.plotting.api.trace;

import java.util.EventListener;

public interface IDownSampleListener extends EventListener{

	/**
	 * Notifies of current downsample rate of an IImageTrace.
	 * 
	 * May be fired multiple times with the same rate and will be fired
	 * during zooming in and out. Therefore work should not be done in this
	 * callback.
	 * 
	 * @param evt
	 */
	void downSampleChanged(DownSampleEvent evt);
}
