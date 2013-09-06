/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.dawnsci.slicing.api.util;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;


/**
 *
 */
class ProgressMonitorWrapper implements IMonitor {

	private IProgressMonitor monitor;

	/**
	 * @param monitor
	 */
	public ProgressMonitorWrapper(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public boolean isCancelled() {
		return monitor.isCanceled();
	}

	@Override
	public void worked(int amount) {
		monitor.worked(amount);
	}
	
	@Override
	public void subTask(String taskName) {
		if (monitor!=null) monitor.subTask(taskName);
	}

}
