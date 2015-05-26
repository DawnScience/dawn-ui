/*-
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogXHandler extends AbstractHandler {

	private final Logger logger = LoggerFactory.getLogger(LogXHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IAxis xAxis;
		try {
			String plotName = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getTitle();
			IPlottingSystem system = PlottingFactory.getPlottingSystem(plotName);
			if (system != null) {
				xAxis = system.getSelectedXAxis();
				if (xAxis != null) {
					xAxis.setLog10(!xAxis.isLog10());
					return true;
				}
			}
		} catch (Exception e) {
			logger.error("Exception while changing log setting on X axis", e);
		}
		return false;
	}

}
