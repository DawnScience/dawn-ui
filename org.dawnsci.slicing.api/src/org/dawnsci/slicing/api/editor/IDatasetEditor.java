/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.slicing.api.editor;

import java.util.Map;

import org.dawb.common.services.IVariableManager;
import org.dawnsci.plotting.api.IPlottingContainer;
import org.dawnsci.slicing.api.data.ICheckableObject;
import org.dawnsci.slicing.api.plot.ISlicePlotUpdateHandler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public interface IDatasetEditor extends IVariableManager, IEditorPart, IPlottingContainer {

	/**
	 * Update the plot with checkables selected by the user.
	 * @param selections
	 * @param participant
	 * @param useTask
	 */
	void updatePlot(final ICheckableObject[]      selections, 
		            final ISlicePlotUpdateHandler participant,
		            final boolean                useTask);

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
	
	/**
	 * A dataset which can be used without loading the data
	 * @param name
	 * @param monitor
	 * @return lazy dataset
	 */
	public ILazyDataset getLazyDataset(String name, IMonitor monitor);

	/**
	 * Return dataset for name
	 * @param name
	 * @param monitor
	 * @return dataset
	 */
	public IDataset getDataset(final String name, final IMonitor monitor);

	/**
	 * Test if editor contains dataset of given name
	 * @param name
	 * @param monitor
	 * @return true if there is a dataset of given name
	 */
	public boolean containsDatasetName(String name, IMonitor monitor);
}
