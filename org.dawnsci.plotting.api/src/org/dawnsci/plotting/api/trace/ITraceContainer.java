/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
