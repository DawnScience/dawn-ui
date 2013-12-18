/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawnsci.slicing.api.util;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;


/**
 *
 */
public class ProgressMonitorWrapper implements IMonitor {

	private IProgressMonitor monitor;

	/**
	 * @param monitor
	 */
	public ProgressMonitorWrapper(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public boolean isCancelled() {
		if (monitor!=null) return monitor.isCanceled();
		return false;
	}

	@Override
	public void worked(int amount) {
		if (monitor!=null) monitor.worked(amount);
	}

	@Override
	public void subTask(String taskName) {
		if (monitor!=null) monitor.subTask(taskName);
	}

}
