/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.services;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.filter.IFilterDecorator;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;

public class PlottingServiceImpl implements IPlottingService {
	
	static {
		System.out.println("Starting plotting service");
	}
	public PlottingServiceImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	@Override
	public IPlottingSystem createPlottingSystem() throws Exception {
		return PlottingFactory.createPlottingSystem();
	}

	@Override
	public IPlottingSystem getPlottingSystem(String plotName) {
		return PlottingFactory.getPlottingSystem(plotName);
	}

	@Override
	public IPlottingSystem getPlottingSystem(String plotName, boolean threadSafe) {
		return PlottingFactory.getPlottingSystem(plotName, threadSafe);
	}

	@Override
	public IToolPageSystem getToolSystem(String plotName) {
		return PlottingFactory.getToolSystem(plotName);
	}

	@Override
	public IPlottingSystem registerPlottingSystem(String plotName,
			IPlottingSystem system) {
		return PlottingFactory.registerPlottingSystem(plotName, system);
	}

	@Override
	public IPlottingSystem removePlottingSystem(String plotName) {
		return PlottingFactory.removePlottingSystem(plotName);
	}

	@Override
	public IFilterDecorator createFilterDecorator(IPlottingSystem system) {
		return PlottingFactory.createFilterDecorator(system);
	}

}
