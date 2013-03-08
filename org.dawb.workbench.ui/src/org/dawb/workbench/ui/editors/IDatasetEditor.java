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

import java.util.Map;

import org.dawb.common.ui.plot.IExpressionPlottingManager;
import org.dawb.common.ui.slicing.ISlicablePlottingPart;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public interface IDatasetEditor extends IExpressionPlottingManager, IEditorPart, ISlicablePlottingPart {

	/**
	 * Update the plot with checkables selected by the user.
	 * @param selections
	 * @param participant
	 * @param useTask
	 */
	void updatePlot(final CheckableObject[]      selections, 
		            final IPlotUpdateParticipant participant,
		            final boolean                useTask);

	/**
	 * It is possible to update the editor input of an IDatasetEditor
	 * @param fileEditorInput
	 */
	void setInput(IEditorInput fileEditorInput);

	/**
	 * Method designed to get the data selected
	 * @return
	 */
	Map<String, IDataset> getSelected();

}
