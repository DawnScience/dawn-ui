/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.tool;

/**
 * Small class that encapsulated window views on a DataSet
 */

public class SelectedWindow {
	
	private double startWindowX;
	private double endWindowX;
	private double startWindowY;
	private double endWindowY;
	private double startWindowX2;
	private double endWindowX2;
	
	/**
	 * Constructor for a SelectedWindow
	 * @param startX x axis start position
	 * @param endX x axis end position
	 * @param startY y axis start position
	 * @param endY y axis end position
	 */
	
	public SelectedWindow(double startX, double endX, double startY, double endY)
	{
		startWindowX = startX;
		endWindowX = endX;
		startWindowY = startY;
		endWindowY = endY;
	}
	
	/**
	 * @param startWindowX
	 */
	public void setStartWindowX(double startWindowX) {
		this.startWindowX = startWindowX;
	}
	
	/**
	 * Get window starting position in X axis
	 * @return window start x-axis
	 */
	public double getStartWindowX() {
		return startWindowX;
	}
	
	/**
	 * @param endWindowX
	 */
	public void setEndWindowX(double endWindowX) {
		this.endWindowX = endWindowX;
	}
	
	/**
	 * Get window ending position in X axis
	 * @return window end x-axis
	 */
	public double getEndWindowX() {
		return endWindowX;
	}
	
	/**
	 * @param startWindowY
	 */
	public void setStartWindowY(double startWindowY) {
		this.startWindowY = startWindowY;
	}
	
	/**
	 * Get window starting position in Y axis
	 * @return window start y-axis
	 */
	public double getStartWindowY() {
		return startWindowY;
	}
	
	/**
	 * @param endWindowY
	 */
	public void setEndWindowY(double endWindowY) {
		this.endWindowY = endWindowY;
	}
	
	/**
	 * Get window ending position in Y axis
	 * @return window end y-axis
	 */
	public double getEndWindowY() {
		return endWindowY;
	}

	public double getEndWindowX2() {
		return endWindowX2;
	}
	
	public double getStartWindowX2() {
		return startWindowX2;
	}
	
	public void setStartWindowX2(double startWindowX) {
		startWindowX2 = startWindowX;
	}
	
	public void setEndWindowX2(double endWindowX) {
		endWindowX2 = endWindowX;
	}
	
}
