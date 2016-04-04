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
	private IPlotActionSystem actionMan;

	public FXPlotActions(FXPlotViewer viewer, IPlottingSystem system) {
		this.viewer = viewer;
		this.actionMan = system.getPlotActionSystem();
	}
	protected void createActions() {
		
		String gridLineGroupNameAction = "javafx.plotting.grid.line.actions";
		actionMan.registerGroup(gridLineGroupNameAction, ManagerType.TOOLBAR);
		
		Action resetTransforms = new Action ("Reset the transforms of the scene", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				viewer.resetSceneTransforms();
			}
		};
		resetTransforms.setChecked(true);
		resetTransforms.setImageDescriptor(Activator.getImageDescriptor("icons/resettransforms.png"));
		actionMan.registerAction(gridLineGroupNameAction, resetTransforms, ActionType.FX3D, ManagerType.TOOLBAR);
		
		
		Action boundingBoxToggle = new Action ("Toggle Bounding Box", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				viewer.setBoundingBoxVisibility(isChecked());
			}
		};
		boundingBoxToggle.setChecked(true);
		boundingBoxToggle.setImageDescriptor(Activator.getImageDescriptor("icons/box.png"));
		actionMan.registerAction(gridLineGroupNameAction, boundingBoxToggle, ActionType.FX3D, ManagerType.TOOLBAR);
		
		
		Action axisToggle = new Action("Toggle Axis Grid",IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				viewer.setAxisGridVisibility(isChecked());
			}
		};
		axisToggle.setChecked(true);
		axisToggle.setImageDescriptor(Activator.getImageDescriptor("icons/axisgrid.png"));
		actionMan.registerAction(gridLineGroupNameAction, axisToggle, ActionType.FX3D, ManagerType.TOOLBAR);
		
		
		Action orthographicToggle = new Action("Toggle Orthographic Camera",IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				viewer.setCameraType(isChecked());
			}
		};
		orthographicToggle.setChecked(false);
		orthographicToggle.setImageDescriptor(Activator.getImageDescriptor("icons/orthographic.png"));
		actionMan.registerAction(gridLineGroupNameAction, orthographicToggle, ActionType.FX3D, ManagerType.TOOLBAR);
		
		Action saveSceneToPng = new Action ("Toggle Bounding Box", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				viewer.saveScreenShotOfSceneToFile();
			}
		};
		saveSceneToPng.setImageDescriptor(Activator.getImageDescriptor("icons/save.png"));
		actionMan.registerAction(gridLineGroupNameAction, saveSceneToPng, ActionType.FX3D, ManagerType.TOOLBAR);
		
	}
}
