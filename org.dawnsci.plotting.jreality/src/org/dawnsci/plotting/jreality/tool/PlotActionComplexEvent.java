/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.tool;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;

/**
 * Complex Plot Action event that not only provides the position on the graph
 * but also gives the underlying dataset and the current region of interest
 */

public class PlotActionComplexEvent extends PlotActionEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IDataset associatedData;
	private SelectedWindow window;
	private AxisValues associateXAxis;
	
	/**
	 * @param tool
	 * @param position
	 * @param dataAssoc
	 * @param xAxis 
	 * @param window 
	 */
	public PlotActionComplexEvent(PlotRightClickActionTool tool, double[] position,
								  IDataset dataAssoc, AxisValues xAxis, 
								  SelectedWindow window) {
		super(tool, position);
		this.associatedData = dataAssoc;
		this.window = window;
		this.associateXAxis = xAxis;
	}

	/**
	 * Get the associated data set
	 * @return the associated data set
	 */
	public IDataset getDataSet() {
		return associatedData;
	}
	
	/**
	 * Get the associated x axis values
	 * @return the associated x axis values if available 
	 */
	public AxisValues getAxisValue() {
		return associateXAxis;
	}
	
	/**
	 * Get the current data window / region of interest / zoom level
	 * @return the data window
	 */
	
	public SelectedWindow getDataWindow() {
		return window;
	}
	
}
