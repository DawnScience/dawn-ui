/*-
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.mapping.ui.datamodel;

import java.util.Arrays;
// Imports from java
import java.util.List;

import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;


// @author Tim Snow

// A class to hold Vector data to be plotted using the mapping perspective in DAWN
public class VectorMapData extends MappedData {
	// Create a few things that will be held by the class 
	AbstractMapData[] vectorMapData;
	IDataset vectors;

	
	// Class initiation
	public VectorMapData(String name, AbstractMapData parent, String path) {
		// Set everything up
		super(name, parent.map, parent.parent, parent.path, false);
		// Create a home for the vector and plot data
		this.vectorMapData = new AbstractMapData[2];
	}

	// For updating just the vector data
	public void updateVectorData (AbstractMapData vectorDirectionLocation, AbstractMapData vectorMagnitudeLocation) {
		// Should anyone want to modify the datasets used first we find them
		vectorMapData[1] = vectorDirectionLocation;
		vectorMapData[0] = vectorMagnitudeLocation;
		
		IDataset d = vectorDirectionLocation.getMap().getTransposedView();
		IDataset m = vectorMagnitudeLocation.getMap().getSliceView().getTransposedView();
		
		padRight(m);
		padRight(d);
		
		vectors = DatasetUtils.concatenate(new IDataset[] {m, d}, 2);
	}
	
	private void padRight(IDataset d) {
		int[] shape = d.getShape();
		int[] padded = new int[shape.length+1];
		padded[shape.length] = 1;
		for (int i = 0; i < shape.length; i++) {
			padded[i] = shape[i];
		}
		d.setShape(padded);
	}

	@Override
	public IDataset getMap() {
		return vectors;
	}

	// Return the axes for a plot
	public List<IDataset> getAxes(){
		
		IDataset map = this.vectorMapData[0].getMap();
		
		IDataset[] ax = MetadataPlotUtils.getAxesAsIDatasetArray(map);
		
		for (IDataset a : ax) {
			a.squeeze();
		}
	
		IDataset tmp = ax[0];
		ax[0] = ax[1];
		ax[1] = tmp;
		
		return Arrays.asList(ax);
	}
	
	
	// Return the axis limits for allowing the vectors to be properly displayed on the image
	public double[] getAxisRange(){
		
		for (AbstractMapData d : vectorMapData) {
			if (d != null) return d.getRange();
		}

		return null;
	}
	
	
	@Override
	public String toString() {
		return "Vector Map";
	}
}