/*-
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


package org.dawnsci.mapping.ui.datamodel;


// Imports from java
import java.util.List;
import java.util.Arrays;


// Imports from org.eclipse.january
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.dataset.DatasetFactory;


// @author Tim Snow


// A class to hold Vector data to be plotted using the mapping perspective in DAWN
public class VectorMapData extends MappedData {
	// Create a few things that will be held by the class
	AbstractMapData[] vectorMapData;
	Dataset plotDataset;
	Dataset vectorDataset;
	Dataset vectorPlotDataset;

	
	// Class initiation
	public VectorMapData(String name, AbstractMapData parent, String path) {
		// Set everything up
		super(name, parent.map, parent.parent, parent.path);
		// Create a home for the vector and plot data
		this.vectorMapData = new AbstractMapData[3];
		// Find out how big the mapped dataset is
		int[] mapDatasetDimensions = parent.map.getShape();
		// Initialise a dataset to stick the data in
		this.plotDataset = DatasetFactory.zeros(new int[]{mapDatasetDimensions[0], mapDatasetDimensions[1]}, Dataset.FLOAT64);
		this.vectorDataset = DatasetFactory.zeros(new int[]{mapDatasetDimensions[0], mapDatasetDimensions[1], 2}, Dataset.FLOAT64);
		this.vectorPlotDataset = DatasetFactory.zeros(new int[]{mapDatasetDimensions[0], mapDatasetDimensions[1], 3}, Dataset.FLOAT64);
		// Extract out the axes
		AxesMetadata axisData = parent.getMap().getFirstMetadata(AxesMetadata.class);
		// Set these to the output dataset
		this.plotDataset.setMetadata(axisData);
		this.vectorDataset.setMetadata(axisData);
		this.vectorPlotDataset.setMetadata(axisData);
	}

	
	// For updating just the plot data
	public void updatePlotData (AbstractMapData plotDataLocation) {
		this.plotDataset = DatasetUtils.convertToDataset(plotDataLocation.getMap());
	}


	// For updating just the vector data
	public void updateVectorData (AbstractMapData vectorDirectionLocation, AbstractMapData vectorMagnitudeLocation) {
		// Should anyone want to modify the datasets used first we find them
		Dataset vectorDirectionDataset = DatasetUtils.convertToDataset(vectorDirectionLocation.getMap());
		Dataset vectorMagnitudeDataset = DatasetUtils.convertToDataset(vectorMagnitudeLocation.getMap());

		int[] dataSize = vectorDirectionDataset.getShape();
		
		for (int xLoop = 0; xLoop < dataSize[0]; xLoop ++){
			for (int yLoop = 0; yLoop < dataSize[1]; yLoop ++) {
				this.vectorDataset.set(vectorMagnitudeDataset.getFloat(xLoop, yLoop), new int[]{xLoop, yLoop, 0});
				this.vectorDataset.set(vectorDirectionDataset.getFloat(xLoop, yLoop), new int[]{xLoop, yLoop, 1});
			}
		}
	}


	// For updating both the plot and vector data
	public void updateVectorPlotData (AbstractMapData plotDataLocation, AbstractMapData vectorDirectionLocation, AbstractMapData vectorMagnitudeLocation) {
		updatePlotData(plotDataLocation);
		updateVectorData(vectorDirectionLocation, vectorMagnitudeLocation);
	}
	

	// If wanted, return the overall dataset
	//Override
	public IDataset getWholeMap() {
		// There's a few options here, so we must go through them all

		if (this.plotDataset != null && this.vectorDataset != null) {
			// If there's both plot and vector data

			// Find the plot data size
			int[] dataSize = this.plotDataset.getShape();


			// Then populate the return dataset with the plot and vector data
			for (int xLoop = 0; xLoop < dataSize[0]; xLoop ++){
				for (int yLoop = 0; yLoop < dataSize[1]; yLoop ++) {
					this.vectorPlotDataset.set(this.plotDataset.getFloat(xLoop, yLoop), new int[]{xLoop, yLoop, 0});
					this.vectorPlotDataset.set(this.vectorDataset.getFloat(xLoop, yLoop, 0), new int[]{xLoop, yLoop, 1});
					this.vectorPlotDataset.set(this.vectorDataset.getFloat(xLoop, yLoop, 1), new int[]{xLoop, yLoop, 2});
				}
			}
		} 
		else if (this.plotDataset != null && this.vectorDataset == null) {
			// If there's just plot data
			
			// Find the plot data size
			int[] dataSize = this.plotDataset.getShape();

			// Then populate the return dataset with the plot data and zeros for the vector data
			for (int xLoop = 0; xLoop < dataSize[0]; xLoop ++){
				for (int yLoop = 0; yLoop < dataSize[1]; yLoop ++) {
					this.vectorPlotDataset.set(this.plotDataset.getFloat(xLoop, yLoop), new int[]{xLoop, yLoop, 0});
					this.vectorPlotDataset.set(0.00, new int[]{xLoop, yLoop, 1});
					this.vectorPlotDataset.set(0.00, new int[]{xLoop, yLoop, 2});
				}
			}			
		} 
		else if (this.plotDataset == null && this.vectorDataset != null) {
			// If there's just vector data

			// Find the vector data size
			int[] dataSize = this.vectorDataset.getShape();

			// Then populate the return dataset with the vector data and zeros for the plot data
			for (int xLoop = 0; xLoop < dataSize[0]; xLoop ++){
				for (int yLoop = 0; yLoop < dataSize[1]; yLoop ++) {
					this.vectorPlotDataset.set(0.00, new int[]{xLoop, yLoop, 0});
					this.vectorPlotDataset.set(this.vectorDataset.getFloat(xLoop, yLoop, 0), new int[]{xLoop, yLoop, 1});
					this.vectorPlotDataset.set(this.vectorDataset.getFloat(xLoop, yLoop, 1), new int[]{xLoop, yLoop, 2});
				}
			}			
		} 
		else if (this.plotDataset == null && this.vectorDataset == null) {
			// If there's no data, return a null dataset
			this.vectorPlotDataset = null;
		}

		// Then return the result, whatever it happened to be
		return this.vectorPlotDataset;		
	}
	

	// If wanted, return just the plot data
	public IDataset getPlotData(){
		return (IDataset) this.plotDataset;
	}
	
	
	// Return the vector map
	@Override
	public IDataset getMap() {
		return (IDataset) this.vectorDataset;
	}

	
	// If wanted, return just the vector data
	public IDataset getVectorData() {
		return (IDataset) this.vectorDataset;
	}
	

	// Return the axes for a plot
	public List<IDataset> getAxes(){
		// Start by creating a place to hold the axes
		IDataset xAxis;
		IDataset yAxis;
		AxesMetadata allAxes;
		
		try {
			// Next try to obtain the axis metadata

			// However, we have to handle perhaps only having plot or vector data
			if (this.plotDataset != null && this.vectorDataset != null) {
				allAxes = this.plotDataset.getFirstMetadata(AxesMetadata.class);
			}
			else if (this.plotDataset != null && this.vectorDataset == null) {
				allAxes = this.plotDataset.getFirstMetadata(AxesMetadata.class);
			}
			else if (this.plotDataset == null && this.vectorDataset != null) {
				allAxes = this.vectorDataset.getFirstMetadata(AxesMetadata.class);
			}
			else {
				throw new DatasetException();
			}

			// If it's available populate the x and y axis datasets
			yAxis = (IDataset) DatasetUtils.sliceAndConvertLazyDataset(allAxes.getAxis(0)[0]).squeeze();
			xAxis = (IDataset) DatasetUtils.sliceAndConvertLazyDataset(allAxes.getAxis(1)[0]).squeeze();
			
			// And return these datasets as a list
			return Arrays.asList(xAxis, yAxis);		
			
		} 
		catch (DatasetException lazySlicingError) {
			// If there's no axis metadata
			System.out.println("An error has occured with loading the lazily loaded data, using default axes");

			// We'll take the size of the plot dataset
			int[] dataSize = this.plotDataset.getShape();

			// Create a linear space axis
			xAxis = DatasetFactory.createLinearSpace(1, dataSize[1] + 1, dataSize[1], Dataset.INT);
			yAxis = DatasetFactory.createLinearSpace(1, dataSize[1] + 1, dataSize[1], Dataset.INT);

			// And return that as a list of datasets
			return Arrays.asList(xAxis, yAxis);		
		}
	}
	
	
	// Return the axis limits for allowing the vectors to be properly displayed on the image
	public double[] getAxisRange(){
		// Start by creating a place to hold the axes
		IDataset xAxis;
		IDataset yAxis;
		AxesMetadata allAxes;
		
		try {
			// Next try to obtain the axis metadata

			// However, we have to handle perhaps only having plot or vector data
			if (this.plotDataset != null && this.vectorDataset != null) {
				allAxes = this.plotDataset.getFirstMetadata(AxesMetadata.class);
			}
			else if (this.plotDataset != null && this.vectorDataset == null) {
				allAxes = this.plotDataset.getFirstMetadata(AxesMetadata.class);
			}
			else if (this.plotDataset == null && this.vectorDataset != null) {
				allAxes = this.plotDataset.getFirstMetadata(AxesMetadata.class);
			}
			else {
				throw new DatasetException();
			}

			// If it's available populate the x and y axis datasets
			yAxis = (IDataset) DatasetUtils.sliceAndConvertLazyDataset(allAxes.getAxis(0)[0]).squeeze();
			xAxis = (IDataset) DatasetUtils.sliceAndConvertLazyDataset(allAxes.getAxis(1)[0]).squeeze();
		} 
		catch (DatasetException lazySlicingError) {
			// If there's no axis metadata
			System.out.println("An error has occured with loading the lazily loaded data, using default axes");

			// We'll take the size of the plot dataset
			int[] dataSize = this.plotDataset.getShape();

			// Create a linear space axis
			xAxis = DatasetFactory.createLinearSpace(1, dataSize[1] + 1, dataSize[1], Dataset.INT);
			yAxis = DatasetFactory.createLinearSpace(1, dataSize[1] + 1, dataSize[1], Dataset.INT);
		}
		
		// Now we have some axes, let's fill the return array
		double[] axisRange = new double[4];
		axisRange[0] = xAxis.getDouble(0);
		axisRange[1] = xAxis.getDouble(xAxis.getSize() - 1);
		axisRange[2] = yAxis.getDouble(0);
		axisRange[3] = yAxis.getDouble(yAxis.getSize() - 1);
		
		// And return it!
		return axisRange;
	}
	
	
//	@Override
//	public IDataset getSpectrum(double x, double y) {
//		return this.plotDataset;
//	}
	
	
	@Override
	public ILazyDataset getData() {
		return this.plotDataset;
	}

	@Override
	public String toString() {
				
		return "Vector Map";
	}
}