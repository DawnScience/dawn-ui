/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
