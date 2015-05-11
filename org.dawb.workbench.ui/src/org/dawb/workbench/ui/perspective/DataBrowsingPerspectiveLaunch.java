/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.perspective;

import org.dawb.workbench.ui.EventTrackerServiceLoader;
import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * Data Browsing Perspective launcher
 *
 * @author Baha El Kassaby
 *
 **/
public class DataBrowsingPerspectiveLaunch implements IWorkbenchWindowActionDelegate {

	@Override
	public void run(IAction action) {
		try {
			PlatformUI.getWorkbench().showPerspective(DataBrowsingPerspective.ID,PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			
			EventTracker tracker = EventTrackerServiceLoader.getService();
			if (tracker != null)
				tracker.track("Data_Browsing_perspective_launch");
		} catch (WorkbenchException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

}
