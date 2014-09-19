/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system;

import org.dawb.common.ui.menu.MenuAction;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.filter.AbstractPlottingFilter;
import org.eclipse.dawnsci.plotting.api.filter.IFilterDecorator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;


/**
 * Class to provide actions to set filter decorators on the plotting system
 * 
 * @author vdp96513
 *
 */
public class PlotFilterActions {

	
	/**
	 * Get filters for 1D data
	 * 
	 * @param system - plotting system to decorate
	 * @return actions
	 */
	public static MenuAction getXYFilterActions(IPlottingSystem system) {
		
		MenuAction menu = new MenuAction("Filters");
		menu.setId("org.dawnsci.plotting.system.XY.Filters");
		
		final IFilterDecorator dec = PlottingFactory.createFilterDecorator(system);
		
		final AbstractPlottingFilter der = new AbstractPlottingFilter() {

			@Override
			public int getRank() {
				return 1;
			}

			protected IDataset[] filter(IDataset x, IDataset y) {

				return new IDataset[]{x, Maths.derivative(DatasetUtils.convertToDataset(x),
						DatasetUtils.convertToDataset(y), 1)};
			}
		};
		
		final AbstractPlottingFilter der2 = new AbstractPlottingFilter() {

			@Override
			public int getRank() {
				return 1;
			}

			protected IDataset[] filter(IDataset x, IDataset y) {

				Dataset xds = DatasetUtils.convertToDataset(x);
				Dataset yds = DatasetUtils.convertToDataset(y);
				
				return new IDataset[]{x, Maths.derivative(xds,Maths.derivative(xds, yds,1),1)};
			}
		};
		
		IAction off = new Action("Off", IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					dec.clear();
				} 
			}
		};
		
		IAction d1 = new Action("1st derivative", IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					dec.clear();
					dec.addFilter(der);
					dec.apply();
				} 
			}
		};
		IAction d2 = new Action("2nd derivative", IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					dec.clear();
					dec.addFilter(der2);
					dec.apply();
				} 
			}
		};
		
		off.setChecked(true);
		menu.add(off);
		menu.add(d1);
		menu.add(d2);
		
		return menu;
	}
	
}
