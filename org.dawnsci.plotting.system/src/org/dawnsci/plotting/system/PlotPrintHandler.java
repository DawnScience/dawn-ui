/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotPrintHandler extends AbstractHandler {

	private final Logger logger = LoggerFactory.getLogger(PlotPrintHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String plotName = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getTitle();
			IPlottingSystem system = PlottingFactory.getPlottingSystem(plotName);
			if (system == null)
				return Boolean.FALSE;
			system.printPlotting();
		} catch (Exception e) {
			logger.error("Error while printing plot", e);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

}
