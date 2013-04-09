/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.editors;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawnsci.plotting.api.PlotType;

import uk.ac.diamond.scisoft.analysis.io.IMetaData;

public interface IPlotUpdateParticipant {

	/**
	 * Set the slicer visible or invisible
	 * @param b
	 */
	void setSlicerVisible(boolean b);

	/**
	 * Get the dimensions for this dataset
	 * @param checkableObject
	 * @return
	 */
	int getDimensionCount(CheckableObject checkableObject);

	/**
	 * The data needed by the slicer when the plot is updated.
	 * @param object
	 * @param filePath
	 * @param dims
	 * @param plottingSystem
	 */
	void setSlicerData(CheckableObject object, 
			           String filePath, 
			           int[] dims,
			           AbstractPlottingSystem plottingSystem);

	/**
	 * The mode of plotting the user prefers when the plot is updated.
	 * @return
	 */
	 PlotType getPlotMode();

}
