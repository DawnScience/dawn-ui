/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.jreality.tool;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

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
