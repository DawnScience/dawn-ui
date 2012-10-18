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

package org.dawnsci.plotting.jreality.core;

import java.util.List;

import org.dawnsci.plotting.jreality.data.ColourImageData;
import org.dawnsci.plotting.jreality.tick.TickFormatting;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import de.jreality.scene.SceneGraphComponent;

/**
 * Interface for any Plotting operation that uses the underlying 3D surrounding
 * needs to implement
 */

public interface IDataSet3DCorePlot {

	/**
	 * Maximum X coordinate axis length
	 */
	public static final double MAXX = 15.0;
	/**
	 * Maximum Y coordinate axis length
	 */
	public static final double MAXY = 15.0;
	/**
	 * Maximum Z coordinate axis length
	 */
	public static final double MAXZ = 15.0;

	/**
	 * Maximum component width in pixels for initial font size
	 */
	
	public final static int FONT_SIZE_PIXELS_WIDTH = 490;
	
	/**
	 * Maximum component height in pixels for initial font size
	 */
	
	public final static int FONT_SIZE_PIXELS_HEIGHT = 475;
	
	/**
	 * Initial Font scale for tick labels
	 */
	
	public final static double FONT_SCALE = 0.0075;

	/**
	 * Initial Font scale for tick labels in software render mode
	 */
	public final static double FONT_SCALE_SOFTWARE = 0.0085;

	/**
	 * Initial Font scale for Axis description labels
	 */
	
	public final static double FONT_AXIS_SCALE = 0.0075;
	
	/**
	 * Initial Font scale for Axis description labels in software render mode 
	 */
	public final static double FONT_AXIS_SCALE_SOFTWARE = 0.0085;

	/**
	 * Overlay scene nodes prefix String so they can easily
	 * distinguished from over scene nodes
	 */
	public final static String OVERLAYPREFIX = "overlayPrim";
	
	
	/**
	 * The scenegraph scene node name String for the actual graph nodes
	 */
	public final static String GRAPHNODENAME = "graph.subGraph";

	/**
	 * The scenegraph scene node name String for the background node
	 */
	
	public final static String BACKGROUNDNODENAME = "background";
	/**
	 * Generate the zCoord Label node, geometry and its appearance
	 * @param comp  SceneGraphComponent node that represents the z-coord labelling
	 */
	public void buildZCoordLabeling(SceneGraphComponent comp);

	/**
	 * Generate the yCoord Label node, geometry and its appearance
	 * @param comp SceneGraphComponent node that represents the y-coord labelling
	 */
	public void buildYCoordLabeling(SceneGraphComponent comp);

	/**
	 * Generate the xCoord Label node, geometry and its appearance
	 * @param comp SceneGraphComponent node that represents the x-coord labelling
	 */

	public void  buildXCoordLabeling(SceneGraphComponent comp);


	/**
	 * @return SceneGraph node that contains all coordinate axis ticks
	 */

	public SceneGraphComponent buildCoordAxesTicks();

	/**
	 * @param datasets
	 *            List of DataSets containing the raw data for the graph
	 * @param graph
	 *            SceneGraphComponent of the graph object
	 * @return SceneGraph node that contains the actual plot
	 */
	public SceneGraphComponent buildGraph(final List<IDataset> datasets, SceneGraphComponent graph);

	/**
	 * Build a bounding box surrounding the whole scene
	 * @return SceneGraphComponent of the bounding box
	 */
	public SceneGraphComponent buildBoundingBox();
	
	/**
	 * Build the coordinate axis for the whole scene
	 * @param axis SceneGraphComponent of the coordinate axis
	 * @return SceneGraphComponent of the coordinate axis
	 */
	
	public SceneGraphComponent buildCoordAxis(SceneGraphComponent axis);
	
	/**
	 * Update the currentEntry in the graph
	 * @param newData
	 */
	
	public void updateGraph(final IDataset newData);
	
	/**
	 * Update multiply entries in the graph
	 * @param datasets
	 */
	
	public void updateGraph(final List<IDataset> datasets);
	
	
	/**
	 * @param newScaling
	 *            newScaling type (see enum in DataSetPlot3D.java)
	 */
	
	public void setScaling(ScaleType newScaling);

	/**
	 * Get the current scaling type
	 * @return the current scaling type
	 */
	public ScaleType getScaling();
	
	/**
	 * @param colourTable
	 *            the colourTable that should be applied to the data
	 * @param graph
	 *            SceneGraph node that contains the plot
	 * @param minValue minimum value
	 * @param maxValue maximum value
	 */

	public void handleColourCast(ColourImageData colourTable, 
								 SceneGraphComponent graph,
								 double minValue,
								 double maxValue);



	/**
	 * Notify the plotter that the underlying display size has been
	 * changed
	 * @param width of the new display size
	 * @param height of the new display size
	 */
	public void notifyComponentResize(int width, int height);

	/**
	 * Set the X axis label
	 * @param label
	 */
	public void setXAxisLabel(String label);
	
	/**
	 * Set the Y axis label
	 * @param label
	 */
	public void setYAxisLabel(String label);
	
	/**
	 * Set the Z axis label
	 * @param label
	 */
	
	public void setZAxisLabel(String label);
	
	/**
	 * Set a title of the graph
	 * @param title new title string
	 */
	public void setTitle(String title);
	
	
	/**
	 * Set an x-axis offset from the dataset starting point
	 * @param offset
	 */
	public void setXAxisOffset(double offset);
	
	/**
	 * Set an y-axis offset from the dataset starting point
	 * @param offset
	 */
	public void setYAxisOffset(double offset);
	
	/**
	 * Set an z-axis offset from the dataset starting point
	 * @param offset
	 */
	
	public void setZAxisOffset(double offset);
	
	/**
	 * Set the axis mode for the different axis
	 * @param xAxis mode for x-axis
	 * @param yAxis mode for y-axis
	 * @param zAxis mode for z-axis
	 */
	public void setAxisModes(AxisMode xAxis, AxisMode yAxis, AxisMode zAxis);
	
	/**
	 * Set x Axis values that map from each entry of the data set as an
	 * x value
	 * @param xAxis x-axis values container
	 * @param numOfDataSets the number of dataSets this axis is referencing to
	 */
	public void setXAxisValues(AxisValues xAxis, int numOfDataSets);
		
	/**
	 * Set y Axis values that map from each entry of the data set as an
	 * y value
	 * @param yAxis y-axis values container
	 */
	public void setYAxisValues(AxisValues yAxis);

	/**
	 * Set z Axis values that map from each entry of the data set as an
	 * y value
	 * @param zAxis z-axis values container
	 */
	public void setZAxisValues(AxisValues zAxis);

	
	/**
	 * Set the x Axis tick label formating mode
	 * @param newFormat new format to be used
	 */
	public void setXAxisLabelMode(TickFormatting newFormat);
	
	/**
	 * Set the y Axis tick label formating mode
	 * @param newFormat new format to be used
	 */
	public void setYAxisLabelMode(TickFormatting newFormat);
	
	/**
	 * Set the z Axis tick label formating mode
	 * @param newFormat new format to be used
	 */
	public void setZAxisLabelMode(TickFormatting newFormat);
	
	/**
	 * Clean up the graph node by removing all children and tools that had been attached to
	 * it in the plotter 
	 */
	public void cleanUpGraphNode();
	
	/**
	 * Set the grid lines on/off for the individual coordinates 
	 * @param xcoord should grid lines for the x axis be shown (true/false)
	 * @param ycoord should grid lines for the y axis be shown (true/false)
	 * @param zcoord should grid lines for the z axis be shown (true/false)
	 */
	
	public void setTickGridLinesActive(boolean xcoord, boolean ycoord, boolean zcoord);
	
	
	/**
	 * Reset the graph view to the previous initial state, undo all
	 * rotation, zooming and panning
	 */
	public void resetView();

	/**
	 * Returns xAxisValues if exists otherwise null
	 * @return list of AxisValues or null if non-existent
	 */	
	public List<AxisValues> getAxisValues();
	
	
	public void toggleErrorBars(boolean xcoord, boolean ycoord, boolean zcoord);
	
}
