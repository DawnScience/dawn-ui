/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.ui.editors.actions;

import org.dawb.common.ui.plot.IPlottingSystemData;
import org.dawb.common.ui.slicing.ISlicablePlottingPart;
import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.ui.IEditorPart;


public class DataSetComponentUtils {

	/**
	 * Trys to find an active DataSetPlotView 
	 * @return DataSetPlotView
	 */
	public static IPlottingSystemData getActiveComponent() {
		
		IPlottingSystemData sets =  null;
		

		IEditorPart editor = EclipseUtils.getActivePage().getActiveEditor();
		if (editor!=null) {
			if (editor instanceof ISlicablePlottingPart) {
				sets = ((ISlicablePlottingPart)editor).getDataSetComponent();
			}

		}
	
		return sets; // Might still be null
	}
}
