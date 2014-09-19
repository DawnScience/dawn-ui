/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system.example;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;

public class TestCommand extends AbstractHandler implements IHandler {

	/**
	 * In order to activate this example command, there is a commented out section
	 * in plugin.xml of this plugin.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
        final IPlottingSystem system = (IPlottingSystem)event.getApplicationContext();
        System.out.println(system.getPlotName());
		return null;
	}

}
