/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.jmx;

import org.eclipse.dawnsci.plotting.api.axis.IAxisSystem;

/**
 * For those that want to reduce dependencies individual interfaces which make up
 * IPlottingSystem may be retrieved here.
 * 
 * @author fcp94556
 *
 */
public class JMXPlottingFactory {

	/**
	 * Gets an IAxisSystem
	 * @param plotName
	 * @param hostName
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public static IAxisSystem getAxisSystem(final String plotName, final String hostName, final int port) throws Exception {
		return new JMXAxisSystem(plotName, hostName, port);
	}
}
