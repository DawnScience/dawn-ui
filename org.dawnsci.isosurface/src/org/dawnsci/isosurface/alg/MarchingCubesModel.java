/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.isosurface.alg;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;

public class MarchingCubesModel extends AbstractOperationModel {
	
	private final ILazyDataset lazyData;
	private final List<? extends IDataset> axes;
	private final double isovalue;
	private final int[] boxSize;
	private final int[] colour;
	private final double opacity;
	private final double isovalueMin = Integer.MAX_VALUE;
	private final double isovalueMax = Integer.MIN_VALUE;
	private final int vertexLimit = 1000000;
	private final String traceID; 
	

	public MarchingCubesModel( 
						ILazyDataset lazyData,
						List<? extends IDataset> axes,
						double isovalue,
						int[] boxSize,
						int[] colour,
						double opacity,
						String traceID)
	{
		this.lazyData   = lazyData   ;
		this.axes       = axes       ;
		this.isovalue   = isovalue   ;
		this.boxSize    = boxSize    ;
		this.colour     = colour     ;
		this.opacity    = opacity    ;
		this.traceID    = traceID    ;
	}
	

	public String getTraceID() {
		return traceID;
	}
	public ILazyDataset getLazyData() {
		return lazyData;
	}
	public double getIsovalue() {
		return isovalue;
	}
	public int[] getBoxSize() {
		return boxSize;
	}
	public double getIsovalueMin() {
		return isovalueMin;
	}
	public double getIsovalueMax() {
		return isovalueMax;
	}
	public int[] getColour(){
		return this.colour;
	}
	public double getOpacity(){
		return this.opacity;
	}
	
	public int getVertexLimit() {
		return vertexLimit;
	}
	public List<? extends IDataset> getAxes() {
		return axes;
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this,  obj);
	}
}
