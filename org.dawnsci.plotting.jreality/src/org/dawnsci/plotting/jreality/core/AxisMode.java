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

package org.dawnsci.plotting.jreality.core;

/**
 * Defines the different axis mode
 */
public enum AxisMode {
	/**
	 * Linear mode so a 1:1 mapping between data point and its x axis
	 */
	LINEAR(1), 
	/**
	 *  Just like linear mode but with an initial offset
	 */
	LINEAR_WITH_OFFSET(2), 
	
	/**
	 * Completely custom each data point mode might have a specific x value
	 */
	CUSTOM(3);

	private int code;
	
	private AxisMode(int code) {
		this.code = code;
	}
	
	/**
	 * Used for saving enum to file.
	 * @return int code
	 */
	public int asInt() {
		return code;
	}
	
	/**
	 * Used for saving enum to file
	 * @param code
	 * @return AxisMode
	 */
	public static AxisMode asEnum(int code) {
		switch (code) {
			case 1: return LINEAR;
			case 2: return LINEAR_WITH_OFFSET;
			case 3: return CUSTOM;
		}
		return null;
	}
}
