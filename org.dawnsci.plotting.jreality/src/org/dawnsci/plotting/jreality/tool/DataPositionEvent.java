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
 *
 */
public class DataPositionEvent extends EventObject implements IDataPositionEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected double position[];
	protected Mode currentMode;
	
	/**
	 * Constructor of a DataPositionEvent 
	 * @param instigater Who is creating this event object?
	 * @param position position coordinates in dataSet space
	 * @param mode current mode (start, drag, end)
	 */
	public DataPositionEvent(Object instigater,
            				 double[] position, 
            				 Mode mode) {
		super(instigater);
		this.position = position.clone();
		this.currentMode = mode;
	}

	/**
	 * Get the current mode
	 * 
	 * @return current mode {@link IDataPositionEvent.Mode}
	 */
	@Override
	public Mode getMode()
	{
		return currentMode;
	}
	
	/**
	 * Get the position in texture coordinates
	 * @return texture coordinates
	 */
	@Override
	public double[] getPosition()
	{
		return position;
	}	
}
