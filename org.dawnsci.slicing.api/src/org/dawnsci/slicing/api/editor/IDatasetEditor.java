/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.slicing.api.editor;

import java.util.Map;

import org.dawnsci.plotting.api.IPlottingContainer;
import org.dawnsci.slicing.api.data.ITransferableDataObject;
import org.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public interface IDatasetEditor extends IEditorPart, IPlottingContainer {

	/**
	 * Update the plot with checkables selected by the user.
	 * @param selections
	 * @param participant
	 * @param useTask
	 */
	void updatePlot(final ITransferableDataObject[]      selections, 
		            final ISliceSystem            sliceSystem,
		            final boolean                 useTask);

	/**
	 * It is possible to update the editor input of an IDatasetEditor
	 * @param fileEditorInput
	 */
	void setInput(IEditorInput fileEditorInput);

	/**
	 * Method designed to get the data selected
	 * @return a map of selected datasets
	 */
	Map<String, IDataset> getSelected();

}
