/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
