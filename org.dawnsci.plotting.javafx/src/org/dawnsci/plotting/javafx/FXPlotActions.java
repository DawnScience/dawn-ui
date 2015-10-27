/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.javafx;

import org.eclipse.dawnsci.plotting.api.ActionType;
import org.eclipse.dawnsci.plotting.api.IPlotActionSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.ManagerType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

class FXPlotActions {

	private FXPlotViewer      viewer;
	private IPlottingSystem   system;
	private IPlotActionSystem actionMan;

	public FXPlotActions(FXPlotViewer viewer, IPlottingSystem system) {
		this.viewer = viewer;
		this.system = system;
		this.actionMan = system.getPlotActionSystem();
	}

	protected void createActions() {
		
		String gridLineGroupNameAction = "javafx.plotting.grid.line.actions";
		actionMan.registerGroup(gridLineGroupNameAction, ManagerType.TOOLBAR);

		
		
		
		Action axisGridToggle = new Action ("Toggle Axis Grid", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				viewer.removeAxisGrid();
			}
		};
		axisGridToggle.setChecked(true);
		axisGridToggle.setImageDescriptor(Activator.getImageDescriptor("icons/orthographic.png"));
		actionMan.registerAction(gridLineGroupNameAction, axisGridToggle, ActionType.FX3D, ManagerType.TOOLBAR);
		
		
		
		
		
		
		Action axisToggle = new Action("Toggle Axes",IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				viewer.addRemoveScaleAxes();
			}
		};
		axisToggle.setChecked(true);
		axisToggle.setImageDescriptor(Activator.getImageDescriptor("icons/axis.png"));
		actionMan.registerAction(gridLineGroupNameAction, axisToggle, ActionType.FX3D, ManagerType.TOOLBAR);
				
	}
}
