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
 * AreaSelectEvent is an event object that gets created when an AreaSelect
 * has been started, is in progress or finished via the AreaSelectTool
 */
public class AreaSelectEvent extends EventObject {

	private static final long serialVersionUID = 3001L;
	
	private double position[];
	private int primitiveID;
	private char areaSelectMode;
	
	/**
	 * Create an AreaSelectEvent
	 * @param tool the AreaSelectTool that fires the event
	 * @param position current mouse position in object space
	 * @param mode current mode of the event 0 - start, 1 - ongoing, 2 - finished
	 */
	public AreaSelectEvent(AreaSelectTool tool, 
						   double[] position, 
						   char mode,
						   int primitiveID)
	{
		super(tool);
		this.position = position.clone();
		areaSelectMode = mode;
		this.primitiveID = primitiveID;
	}
	
	/**
	 * Return the current mode
	 * @return current mode 
	 */
	public char getMode()
	{
		return areaSelectMode;
	}
	
	/**
	 * @return the position of the mouse in object space
	 */
	public double[] getPosition()
	{
		return position;
	}	
	
	/**
	 * @return x convenience method
	 */
	public double getX() {
		return position[0];
	}
	/**
	 * @return y convenience method
	 */
	public double getY() {
		return position[1];
	}
	
	public int getPrimitiveID() {
		return primitiveID;
	}
}
