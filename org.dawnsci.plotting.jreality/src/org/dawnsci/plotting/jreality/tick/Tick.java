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

package org.dawnsci.plotting.jreality.tick;

/**
 *
 */
public class Tick {

	private String tickName;
	private double tickValue;
	private double xCoord;
	private double yCoord;
	private double zCoord;
	
	/**
	 * @param tickName
	 */
	public void setTickName(String tickName) {
		this.tickName = tickName;
	}
	
	/**
	 * @return the tick name
	 */
	public String getTickName() {
		return tickName;
	}
	
	/**
	 * @param value
	 */
	public void setTickValue(double value) {
		this.tickValue = value;
	}
	
	/**
	 * @return the tick value
	 */
	public double getTickValue() {
		return tickValue;
	}
	
	/**
	 * @param xCoord
	 */
	public void setXCoord(double xCoord) {
		this.xCoord = xCoord;
	}
	
	/**
	 * @return the x-coordinate
	 */
	public double getXCoord() {
		return xCoord;
	}
	
	/**
	 * @param yCoord
	 */
	public void setYCoord(double yCoord) {
		this.yCoord = yCoord;
	}
	
	/**
	 * @return the y-coordinate 
	 */
	public double getYCoord() {
		return yCoord;
	}
	
	/**
	 * @param zCoord
	 */
	public void setZCoord(double zCoord) {
		this.zCoord = zCoord;
	}
	
	/**
	 * @return the z-coordinate
	 */
	public double getZCoord() {
		return zCoord;
	}
	
}
