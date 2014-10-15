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

import org.dawnsci.isosurface.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;

public class MarchingCubesModel extends AbstractOperationModel {

	
	private ILazyDataset lazyData;
	private double isovalue;
	private int[] boxSize;
	private double isovalueMin = Integer.MAX_VALUE;
	private double isovalueMax = Integer.MIN_VALUE;
	private int    vertexLimit = 1000000;
	
	public ILazyDataset getLazyData() {
		return lazyData;
	}
	public void setLazyData(ILazyDataset lz) {
		if (lazyData!=lz) computeExtents(lz);
		this.lazyData = lz;
	}
	
	private void computeExtents(ILazyDataset lz) {
		// If we have just set new data, reset the values for box size and isovalue
		this.boxSize  = getEstimatedBoxSize(lz);
		this.isovalue = getEstimatedIsovalue(lz); // Also estimates max and min slider range
	}

	/**
	 * Method for computing the default box size
	 */
    private static int[] getEstimatedBoxSize(ILazyDataset lz) {
		int[] defaultBoxSize= new int[] {(int) Math.max(1, Math.ceil(lz.getShape()[2]/20.0)),
	                                     (int) Math.max(1, Math.ceil(lz.getShape()[1]/20.0)),
	                                     (int) Math.max(1, Math.ceil(lz.getShape()[0]/20.0))};
		
		return defaultBoxSize;
	}


	/**
	 * Method for computing the default isovalue 
	 */
	private double getEstimatedIsovalue(ILazyDataset lz) {
		// TODO Auto-generated method stub
		IDataset slicedImage;
		
		
		slicedImage = lz.getSlice(new int[] { lz.getShape()[0]/3, 0,0}, 
				                              new int[] {1+lz.getShape()[0]/3, lz.getShape()[1], lz.getShape()[2]},
				                              new int[] {1,1,1});
		
		final IImageService service = (IImageService)Activator.getService(IImageService.class);
		double[] stats = service.getFastStatistics(new ImageServiceBean((Dataset)slicedImage, HistoType.MEAN));
		
		if(stats[0]<isovalueMin){
			isovalueMin = stats[0];
		}
		
		if(stats[1]>isovalueMax){
			isovalueMax = stats[1];
		}
		
		slicedImage = lz.getSlice(new int[] { lz.getShape()[0]/2, 0,0}, 
                new int[] {1+ lz.getShape()[0]/2, lz.getShape()[1], lz.getShape()[2]},
                new int[] {1,1,1});
		
        stats = service.getFastStatistics(new ImageServiceBean((Dataset)slicedImage, HistoType.MEAN));
		
		if(stats[0]<isovalueMin){
			isovalueMin = stats[0];
		}
		
		if(stats[1]>isovalueMax){
			isovalueMax = stats[1];
		}
		
		slicedImage = lz.getSlice(new int[] { 2*lz.getShape()[0]/3, 0,0}, 
                new int[] {1 + 2*lz.getShape()[0]/3, lz.getShape()[1], lz.getShape()[2]},
                new int[] {1,1,1});
		
		stats = service.getFastStatistics(new ImageServiceBean((Dataset)slicedImage, HistoType.MEAN));
		
		if(stats[0]<isovalueMin){
			isovalueMin = stats[0];
		}
		
		if(stats[1]>isovalueMax){
			isovalueMax = stats[1];
		}
		
		return (isovalueMin + isovalueMax)/2d;
		
	}

	public double getIsovalue() {
		return isovalue;
	}
	public void setIsovalue(double isovalue) {
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
