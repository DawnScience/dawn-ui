/*-
 * Copyright 2014 Diamond Light Source Ltd.
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

package org.dawnsci.plotting.roi;

import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * This is a region of interest selection object for the 3D surface plotting 
 * 
 */
public class SurfacePlotROI extends RectangularROI {

	/**
	 * 
	 */
	private static final long serialVersionUID = -85518689914929874L;
	
	private int startX;
	private int startY;
	private int endX;
	private int endY;
	private int xSamplingMode;
	private int ySamplingMode;
	private int xAspect;
	private int yAspect;
	private int xBinShape = 1;
	private int yBinShape = 1;
	private int lowerClipping;
	private int upperClipping;
	private boolean isClippingApplied;

	/**
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param xSamplingMode
	 * @param ySamplingMode
	 * @param xAspect
	 * @param yAspect
	 */
	public SurfacePlotROI(int startX, int startY,
			              int endX, int endY,
			              int xSamplingMode, int ySamplingMode,
			              int xAspect, int yAspect) {
		
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.xSamplingMode = xSamplingMode;
		this.ySamplingMode = ySamplingMode;
		this.xAspect = xAspect;
		this.yAspect = yAspect;
		spt = new double[]{startX,startY};
	}
	
	@Override
	public String toString() {
		return "("+startX+", "+startY+")("+endX+", "+endY+")";
	}

	@Override
	public void setPoint(int startX, int startY) {
		this.startX = startX;
		this.startY = startY;
	}

	public int getStartX() {
		return startX; 
	}
	
	public int getStartY() {
		return startY;
	}
	
	public int getEndX() {
		return endX;
	}
	
	public int getEndY() {
		return endY;
	}
	
	public int getXSamplingMode() {
		return xSamplingMode;
	}
	
	public int getYSamplingMode() {
		return ySamplingMode;
	}
	
	public int getXAspect() {
		return xAspect;
	}
	
	public int getYAspect() {
		return yAspect;
	}

	public int getXBinShape() {
		return xBinShape;
	}
	
	public int getYBinShape() {
		return yBinShape;
	}

	public void setXBinShape(int xBinShape) {
		this.xBinShape = xBinShape;
	}
	
	public void setYBinShape(int yBinShape) {
		this.yBinShape = yBinShape;
	}

	public int getLowerClipping() {
		return lowerClipping;
	}

	public int getUpperClipping() {
		return upperClipping;
	}

	public void setLowerClipping(int lowerClipping) {
		this.lowerClipping = lowerClipping;
	}

	public void setUpperClipping(int upperClipping) {
		this.upperClipping = upperClipping;
	}

	public void setIsClippingApplied(boolean isClippingApplied) {
		this.isClippingApplied = isClippingApplied;
	}

	public boolean isClippingApplied() {
		return isClippingApplied;
	}
}
