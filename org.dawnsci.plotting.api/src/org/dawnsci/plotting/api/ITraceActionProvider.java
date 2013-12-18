/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.api;

import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.action.IContributionManager;

public interface ITraceActionProvider {
	/**
	 * Creates the trace actions (add line, box etc.) in the IContributionManager
	 * @param toolBarManager
	 * @param imageTrace
	 * @param system
	 */
	public void fillTraceActions(IContributionManager toolBarManager, ITrace imageTrace, IPlottingSystem system);

}
