/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.services;

import org.eclipse.dawnsci.plotting.api.IPlotRegistrationListener;
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
	public <T> IPlottingSystem<T> createPlottingSystem() throws Exception {
		return PlottingFactory.createPlottingSystem();
	}

	@Override
	public <T> IPlottingSystem<T> getPlottingSystem(String plotName) {
		return PlottingFactory.getPlottingSystem(plotName);
	}

	@Override
	public <T> IPlottingSystem<T> getPlottingSystem(String plotName, boolean threadSafe) {
		return PlottingFactory.getPlottingSystem(plotName, threadSafe);
	}

	@Override
	public IToolPageSystem getToolSystem(String plotName) {
		return PlottingFactory.getToolSystem(plotName);
	}

	@Override
	public <T> IPlottingSystem<T> registerPlottingSystem(String plotName, IPlottingSystem<T> system) {
		return PlottingFactory.registerPlottingSystem(plotName, system);
	}

	@Override
	public <T> IPlottingSystem<T> removePlottingSystem(String plotName) {
		return PlottingFactory.removePlottingSystem(plotName);
	}

	@Override
	public <T> IFilterDecorator createFilterDecorator(IPlottingSystem<T> system) {
		return PlottingFactory.createFilterDecorator(system);
	}

	@Override
	public void addRegistrationListener(IPlotRegistrationListener l) {
		PlottingFactory.addRegistrationListener(l);
	}

	@Override
	public void removeRegistrationListener(IPlotRegistrationListener l) {
		PlottingFactory.removeRegistrationListener(l);
	}

}
