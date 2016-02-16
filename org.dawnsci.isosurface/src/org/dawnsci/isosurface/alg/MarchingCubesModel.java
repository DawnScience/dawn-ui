/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.isosurface.alg;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;

public class MarchingCubesModel extends AbstractOperationModel {
	
	private ILazyDataset lazyData;
	private double isovalue;
	private int[] boxSize;
	private int[] colour = {0,0,0};
	private double opacity = 1.0;
	private double isovalueMin = Integer.MAX_VALUE;
	private double isovalueMax = Integer.MIN_VALUE;
	private int    vertexLimit = 1000000;
	private String traceID; 
	private String name;
	

	public MarchingCubesModel( 
						ILazyDataset lazyData,
						double isovalue,
						int[] boxSize,
						int[] colour,
						double opacity,
						String traceID,
						String name)
	{
		this.lazyData      = lazyData   ;
		this.isovalue      = isovalue   ;
		this.boxSize       = boxSize    ;
		this.colour        = colour     ;
		this.opacity       = opacity    ;
		this.traceID       = traceID    ;
		this.name		   = name		;
	}
	

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTraceID() {
		return traceID;
	}
	public void setTraceID(String traceID) {
		this.traceID = traceID;
	}
	public void setIsovalue(double isovalue) {
		this.isovalue = isovalue;
	}
	public void setColour(int[] colour) {
		this.colour = colour;
	}
	public ILazyDataset getLazyData() {
		return lazyData;
	}
	public void setLazyData(ILazyDataset lz) {
		this.lazyData = lz;
	}
	public double getIsovalue() {
		return isovalue;
	}
	public void setIsovalue(Double isovalue) {
		if (isovalue != null)
			this.isovalue = isovalue;
	}
	public int[] getBoxSize() {
		return boxSize;
	}
	public void setBoxSize(int[] boxSize) {
		this.boxSize = boxSize;
	}
	public double getIsovalueMin() {
		return isovalueMin;
	}
	public void setIsovalueMin(double isovalueMin) {
		this.isovalueMin = isovalueMin;
	}
	public double getIsovalueMax() {
		return isovalueMax;
	}
	public void setIsovalueMax(double isovalueMax) {
		this.isovalueMax = isovalueMax;
	}
	public void setColour(int r, int g, int b){
		this.colour = new int[]{r%256, g%256, b%256};
	}	
	public int[] getColour(){
		return this.colour;
	}
	public void setOpacity(double newOpacity){
		this.opacity = newOpacity;
	}
	public double getOpacity(){
		return this.opacity;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(boxSize);
		long temp;
		temp = Double.doubleToLongBits(isovalue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(isovalueMax);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(isovalueMin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((lazyData == null) ? 0 : lazyData.hashCode());
		result = prime * result + vertexLimit;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarchingCubesModel other = (MarchingCubesModel) obj;
		if (!Arrays.equals(boxSize, other.boxSize))
			return false;
		if (Double.doubleToLongBits(isovalue) != Double
				.doubleToLongBits(other.isovalue))
			return false;
		if (Double.doubleToLongBits(isovalueMax) != Double
				.doubleToLongBits(other.isovalueMax))
			return false;
		if (Double.doubleToLongBits(isovalueMin) != Double
				.doubleToLongBits(other.isovalueMin))
			return false;
		if (lazyData == null) {
			if (other.lazyData != null)
				return false;
		} else if (!lazyData.equals(other.lazyData))
			return false;
		if (vertexLimit != other.vertexLimit)
			return false;
		return true;
	}
	public int getVertexLimit() {
		return vertexLimit;
	}
	public void setVertexLimit(int vertexLimit) {
		this.vertexLimit = vertexLimit;
	}
	


}
