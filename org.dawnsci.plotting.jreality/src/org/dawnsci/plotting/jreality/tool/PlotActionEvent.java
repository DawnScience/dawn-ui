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

import java.util.EventObject;

/**
 * PlotActionEvent. A PlotActionEvent will be either be created when a right click
 * action occurs or just by hovering the mouse over the graph elements
 */

public class PlotActionEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double position[];
	private int dataPosition[];
	private int graphNr;
	
	/**
	 * @param tool
	 * @param position
	 */
	public PlotActionEvent(PlotActionTool tool, double[] position) {
		super(tool);
		this.position = position.clone();
		this.graphNr = -1;
	}

	/**
	 * @param tool
	 * @param position
	 * @param graphNr
	 */
	public PlotActionEvent(PlotActionTool tool, double[] position, int graphNr) {
		this(tool,position);
		this.graphNr = graphNr;
	}
	
	/**
	 * Set the actual data position in the underlying Dataset 
	 * @param position the actual data position
	 */
	
	public void setDataPosition(int[] position)
	{
		this.dataPosition = position.clone();
	}
	
	/**
	 * Get the actual data position in the underlying Dataset 
	 * @return the actual data position
	 */
	
	public int[] getDataPosition()
	{
		return dataPosition;		
	}
	/**
	 * Get the selected graph number 
	 * @return the selected graph number if available otherwise -1
	 */
	public int getSelectedGraphNr() {
		return graphNr;
	}

	/**
	 * @return the position of the mouse in object space
	 */
	
	public double[] getPosition()
	{
		return position;
	}	
	
	@Override
	public String toString() {
		return "( "+position[0]+", "+position[1]+" )";
	}
	
}
