/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
