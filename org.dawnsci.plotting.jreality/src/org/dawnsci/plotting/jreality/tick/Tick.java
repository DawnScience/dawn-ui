/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
