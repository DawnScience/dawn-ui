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

import org.eclipse.dawnsci.analysis.api.diffraction.NumberOfSymmetryFolds;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.ComplexDoubleDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;


/**
 * A class to hold Vector data to be plotted using the mapping perspective in DAWN
 * @author Tim Snow
 *
 */
public class VectorMapData extends MappedData {
	
	AbstractMapData[] vectorMapData;
	IDataset vectors;
    boolean showAsComplex = false;
    private NumberOfSymmetryFolds folds = null;

	public VectorMapData(String name, AbstractMapData parent, String path, boolean showAsComplex, NumberOfSymmetryFolds folds) {
		super(name, parent.map, parent.parent, parent.path, false);
		this.vectorMapData = new AbstractMapData[2];
		this.showAsComplex = showAsComplex;
		this.folds = folds;
	}
    
	public VectorMapData(String name, AbstractMapData parent, String path) {
		this(name, parent, path, false, null);
	}
	
	
	// For updating just the vector data
	public void updateVectorData (AbstractMapData vectorDirectionLocation, AbstractMapData vectorMagnitudeLocation) {
		// Should anyone want to modify the datasets used first we find them
		vectorMapData[1] = vectorDirectionLocation;
		vectorMapData[0] = vectorMagnitudeLocation;
		
		
		
		if (isShowAsComplex()) {
			List<IDataset> axes = getAxes();
			try {
				IDataset d = vectorDirectionLocation.getMap();
				IDataset m = vectorMagnitudeLocation.getMap();
				
				if (folds != null) {
					d = Maths.multiply(d, 2.0*folds.getFoldsOfSymmetry());
				}
				
				d = Maths.toRadians(d);
				
				Dataset x = Maths.multiply(m,Maths.cos(d));
				Dataset y = Maths.multiply(m,Maths.sin(d));

				AxesMetadata md = MetadataFactory.createMetadata(AxesMetadata.class, 2);
				md.setAxis(0, axes.get(1));
				md.setAxis(1, axes.get(0));
				vectors = DatasetFactory.createComplexDataset(ComplexDoubleDataset.class, y,x);
				vectors.setMetadata(md);
			} catch (MetadataException e) {
			}
		} else {
			IDataset d = vectorDirectionLocation.getMap().getTransposedView();
			IDataset m = vectorMagnitudeLocation.getMap().getSliceView().getTransposedView();
			padRight(m);
			padRight(d);
			vectors = DatasetUtils.concatenate(new IDataset[] {m, d}, 2);
		}
	}
	
	public boolean isShowAsComplex() {
		return showAsComplex;
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