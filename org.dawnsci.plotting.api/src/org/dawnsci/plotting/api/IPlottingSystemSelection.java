/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.plotting.api;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public interface IPlottingSystemSelection {
	
	/**
	 * Select some data and return it for future use.
	 * @param name
	 * @param clearOthers
	 * @return
	 */
	public IDataset setDatasetSelected(final String name, final boolean clearOthers);
	

	/**
	 * This method can be used to select all 1D plots. It is not part of the AbstractPlottingSystem
	 * because plots are selected outside of the main plotting system. @see PlotDataView and pages of it.
	 * 
	 * @param b
	 */
	public void setAll1DSelected(boolean allSelected);
}
