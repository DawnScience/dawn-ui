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
