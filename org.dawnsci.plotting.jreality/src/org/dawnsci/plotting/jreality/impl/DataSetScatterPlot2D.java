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

package org.dawnsci.plotting.jreality.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.jreality.core.AxisMode;
import org.dawnsci.plotting.jreality.tool.SelectedWindow;
import org.dawnsci.plotting.jreality.util.ErrorHelpers;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.AbstractViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

/**
 *
 */
public class DataSetScatterPlot2D extends DataSet3DPlot1D {

	private List<AxisValues> yAxes = Collections
	.synchronizedList(new LinkedList<AxisValues>());

	private LinkedList<DefaultPointShader> graphPointShaders;
	private double dataMin;
	private double dataMax;
	private static final double MAXRADII = 64.0;
	
	SceneGraphComponent xErrors;
	SceneGraphComponent yErrors;
	
	public DataSetScatterPlot2D(AbstractViewerApp app, 
								Composite plotArea, 
								Cursor defaultCursor,
								Plot1DGraphTable colourTable, 
								boolean hasJOGL) {
		super(app, plotArea, defaultCursor, colourTable, hasJOGL);
		xAxis = AxisMode.CUSTOM;
		yAxis = AxisMode.CUSTOM;
		graphPointShaders = new LinkedList<DefaultPointShader>();
	}

	@Override
	protected void determineRanges(List<IDataset> datasets) {
		Iterator<IDataset> iter = datasets.iterator();
		Iterator<AxisValues> xaxisIter = xAxes.iterator();
		Iterator<AxisValues> yaxisIter = yAxes.iterator();
		globalYmin = Float.MAX_VALUE;
		globalYmax = -Float.MAX_VALUE;
		globalXmin = Float.MAX_VALUE;
		globalXmax = -Float.MAX_VALUE;
		globalRealXmin = Float.MAX_VALUE;
		globalRealXmax = -Float.MAX_VALUE;
		dataMin = Float.MAX_VALUE;
		dataMax = -Float.MAX_VALUE;
		while (iter.hasNext()) {
			IDataset set = iter.next();
			AxisValues xAxis = xaxisIter.next();
			AxisValues yAxis = yaxisIter.next();
			globalRealXmin = Math.min(globalRealXmin, xAxis.getMinValue());
			globalRealXmax = Math.max(globalRealXmax, xAxis.getMaxValue());
			globalXmax = globalRealXmax;
			globalYmin = Math.min(globalYmin, yAxis.getMinValue());
			globalYmax = Math.max(globalYmax, yAxis.getMaxValue());
			dataMin = Math.min(set.min().doubleValue(), dataMin);
			dataMax = Math.max(set.max().doubleValue(), dataMax);
		}
		// check on yAxis is in offset mode if yes it will be added to the
		// minimum
		sanityCheckMinMax();
		// Now potential overwrite the global min/max depending on what
		// the tick labels come up with
		buildTickLists();		
	}	
	
	private PointSet createGraphGeometry(IDataset plotSet, AxisValues xAxis, AxisValues yAxis) {
		int graphSize = plotSet.getSize();
		PointSetFactory pFactory = new PointSetFactory();
		double[] coords = new double[graphSize * 3];
		double[] radii = {};
		if (dataMin != dataMax)
			radii = new double[graphSize];
		Iterator<Double> xValues = xAxis.iterator();
		Iterator<Double> yValues = yAxis.iterator();
		int counter = 0;
		while (xValues.hasNext()) {
			double xValue = xValues.next();
			double yValue = yValues.next();
			coords[counter * 3] = ((xValue - graphXmin))
				* ((MAXX - xInset) / (graphXmax - graphXmin));					
			coords[counter * 3 + 1] = (yValue - graphYmin) * ((MAXY-yInset) / (graphYmax - graphYmin));
			coords[counter * 3 + 2] = 0.0;
			if (dataMin != dataMax) {
				if (dataMax > MAXRADII)
					radii[counter] = MAXRADII * plotSet.getDouble(counter) / dataMax;
				else
					radii[counter] = plotSet.getDouble(counter);
			}
			counter++;
		}
		pFactory.setVertexCount(graphSize);
		if (dataMin != dataMax) 
			pFactory.setVertexRelativeRadii(radii);
		pFactory.setVertexCoordinates(coords);
		pFactory.update();
		return pFactory.getPointSet();
	}
	
	@Override
	public SceneGraphComponent buildGraph(List<IDataset> datasets,
			SceneGraphComponent graph) {
		assert datasets.size() > 0;
		if (graph != null) {
			graphGroupNode = SceneGraphUtility
					.createFullSceneGraphComponent("groupNode");
			this.sets = datasets;
			this.graph = graph;
			determineRanges(datasets);
			Iterator<IDataset> iter = datasets.iterator();
			Iterator<AxisValues> xaxisIter = xAxes.iterator();
			Iterator<AxisValues> yaxisIter = yAxes.iterator();
			currentSelectWindow = new SelectedWindow(0, (int) globalXmax, 0, 0);
			numGraphs = 0;
			while (iter.hasNext()) {
				IDataset currentDataSet = iter.next();
				SceneGraphComponent subGraph = SceneGraphUtility
						.createFullSceneGraphComponent(GRAPHNAMEPREFIX
								+ numGraphs);
				subGraphs.add(subGraph);
				AxisValues xAxis = xaxisIter.next();
				AxisValues yAxis = yaxisIter.next();
				
				subGraph.setGeometry(createGraphGeometry(currentDataSet,
							xAxis,yAxis));
				Appearance graphAppearance = new Appearance();
				subGraph.setAppearance(graphAppearance);
				DefaultGeometryShader dgs = ShaderUtility
						.createDefaultGeometryShader(graphAppearance, true);
				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
						+ CommonAttributes.TUBES_DRAW, false);
				graphAppearance.setAttribute(CommonAttributes.POINT_SHADER
						+ "." + CommonAttributes.SPHERES_DRAW, false);
				graphAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE,false);
				graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
				dgs.setShowFaces(false);
				dgs.setShowLines(false);
				dgs.setShowPoints(true);
				DefaultPointShader dps = (DefaultPointShader)dgs.createPointShader("default");
				graphPointShaders.add(dps);
	//			DefaultLineShader dls = (DefaultLineShader) dgs
	//					.createLineShader("default");
				Plot1DAppearance plotApp = graphColours
						.getLegendEntry(numGraphs);
	//			plotApp.updateGraph(dls, dgs);
				dps.setSpheresDraw(false);
				dps.setDiffuseColor(plotApp.getColour());
				if (dataMin == dataMax) 
					dps.setPointSize(dataMin);
				else
					dps.setPointSize(1.0);
				graphShaders.add(dgs);
				
				addErrorBarNodesToPointNode(xAxis, yAxis, subGraph);
				
				graphGroupNode.addChild(subGraph);
				numGraphs++;
			}
			buildClipPlanes(graph);
			resizeAndPositionNodes();
			Camera sceneCamera = CameraUtility
					.getCamera(app.getCurrentViewer());
			sceneCamera.setFieldOfView(56.5f);

			// now add an invisible background component
			// that we use for the picking so we can easily get
			// object space coordinates for our mouse for the AreaSelection Tool
			// make sure that this node gets removed from the graph node
			// when DataSet3DPlot1D is no longer the active plotter
			// and the tool as well

			if (background == null) {
				buildBackground();
				graph.addChild(background);
				graph.addTool(tool);
	//			if (actionToolEnabled) graph.addTool(actionTool);
	//			if (rightClickActionToolEnabled) {
	//				graph.addTool(rightClickActionTool);
	//			}
			}

			if (areaSelection == null) {
				buildAreaSelection();
			}

			if (xTicks != null)
				xTicks.setGeometry(createXTicksGeometry());
			if (yTicks != null)
				yTicks.setGeometry(createYTicksGeometry());

		}
		return graph;
	}
	
	private void addErrorBarNodesToPointNode(AxisValues xAxis, AxisValues yAxis, SceneGraphComponent subGraph ) {		
		
		// now set up all the error axis.		
		double[] xPoints = ErrorHelpers.extractAndScale(xAxis.toDataset(), graphXmin, MAXX-xInset, graphXmax);
		double[] yPoints = ErrorHelpers.extractAndScale(yAxis.toDataset(), graphYmin, (MAXY-yInset), graphYmax);
		double[] zPoints = ErrorHelpers.constantPoints(0.0, xAxis.size());
		double[] xErrorPoints = ErrorHelpers.extractAndScaleError(xAxis.toDataset(), graphXmin, MAXX-xInset, graphXmax);
		double[] xErrorBarDirection = new double[] {1.0,0.0,0.0};
		double[] xErrorBarWidthDirection = new double[] {0.0,1.0,0.0};
		double   errorBarWidth = (MAXX-xInset)/(xAxis.size()*4.0);
		
		double[] yErrorPoints = ErrorHelpers.extractAndScaleError(yAxis.toDataset(), graphYmin, (MAXY-yInset), graphYmax);
		double[] yErrorBarDirection = new double[] {0.0,1.0,0.0};
		double[] yErrorBarWidthDirection = new double[] {1.0,0.0,0.0};
		
		// Finally create the scene graph nodes
		xErrors = ErrorHelpers.createErrorNode(xPoints, yPoints, zPoints, xErrorPoints, errorBarWidth, xErrorBarDirection, xErrorBarWidthDirection);
		yErrors = ErrorHelpers.createErrorNode(xPoints, yPoints, zPoints, yErrorPoints, errorBarWidth, yErrorBarDirection, yErrorBarWidthDirection);
		
		// then add them to the node which is the points of the data.
		subGraph.addChild(xErrors);
		subGraph.addChild(yErrors);
	}
	
	
	private void updateErrorBarNodesToPointNode(AxisValues xAxis, AxisValues yAxis) {		
		
		// now set up all the error axis.		
		double[] xPoints = ErrorHelpers.extractAndScale(xAxis.toDataset(), graphXmin, MAXX-xInset, graphXmax);
		double[] yPoints = ErrorHelpers.extractAndScale(yAxis.toDataset(), graphYmin, MAXY, graphYmax);
		double[] zPoints = ErrorHelpers.constantPoints(0.0, xAxis.size());
		double[] xErrorPoints = ErrorHelpers.extractAndScaleError(xAxis.toDataset(), graphXmin, MAXX-xInset, graphXmax);
		double[] xErrorBarDirection = new double[] {1.0,0.0,0.0};
		double[] xErrorBarWidthDirection = new double[] {0.0,1.0,0.0};
		double   errorBarWidth = (MAXX-xInset)/(xAxis.size()*4.0);
		
		double[] yErrorPoints = ErrorHelpers.extractAndScaleError(yAxis.toDataset(), graphYmin, MAXY, graphYmax);
		double[] yErrorBarDirection = new double[] {0.0,1.0,0.0};
		double[] yErrorBarWidthDirection = new double[] {1.0,0.0,0.0};
		
		// Finally create the scene graph nodes
		ErrorHelpers.updateErrorNode(xErrors, xPoints, yPoints, zPoints, xErrorPoints, errorBarWidth, xErrorBarDirection, xErrorBarWidthDirection);
		ErrorHelpers.updateErrorNode(yErrors, xPoints, yPoints, zPoints, yErrorPoints, errorBarWidth, yErrorBarDirection, yErrorBarWidthDirection);
	}
	
	
	
	public void addAxises(AxisValues xAxis, AxisValues yAxis) {
		xAxes.add(xAxis);
		yAxes.add(yAxis);		
	}
	
	public void replaceAxises(List<AxisValues> xAxis, List<AxisValues> yAxis) {
		xAxes.clear();
		yAxes.clear();
		xAxes.addAll(xAxis);
		yAxes.addAll(yAxis);
	}
	
	
	@Override
	public void setYAxisValues(AxisValues yAxis) {
		if (yAxes.size() > 0)
			yAxes.set(0, yAxis);
		else
			yAxes.add(yAxis);
	}
	
	@Override
	public void addGraphNode() {
		if (graph != null) {
			buildAdditionalGraphNode();
		}
	}
	
	@Override
	protected SceneGraphComponent buildAdditionalGraphNode() {
		SceneGraphComponent subGraph = SceneGraphUtility
		.createFullSceneGraphComponent(GRAPHNAMEPREFIX
				+ numGraphs);
		subGraphs.add(subGraph);
		Appearance graphAppearance = new Appearance();
		subGraph.setAppearance(graphAppearance);
		DefaultGeometryShader dgs = ShaderUtility
				.createDefaultGeometryShader(graphAppearance, true);
		graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.TUBES_DRAW, false);
		graphAppearance.setAttribute(CommonAttributes.POINT_SHADER
				+ "." + CommonAttributes.SPHERES_DRAW, false);
		graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		graphAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
		dgs.setShowFaces(false);
		dgs.setShowLines(false);
		dgs.setShowPoints(true);
		Plot1DAppearance plotApp = graphColours
			.getLegendEntry(numGraphs);	
		DefaultPointShader dps = (DefaultPointShader)dgs.createPointShader("default");
		dps.setSpheresDraw(false);
		dps.setPointSize(4.0);
		dps.setDiffuseColor(plotApp.getColour());
		graphShaders.add(dgs);
		graphPointShaders.add(dps);
		graphGroupNode.addChild(subGraph);		
		numGraphs++;
		return subGraph;	
	}
	
	@Override
	public void removeLastGraphNode() {
		if (graph != null) {
			assert graphPointShaders.size() > 1;
			graphPointShaders.remove(graphPointShaders.size() - 1);
			graphShaders.remove(graphShaders.size() - 1);
			SceneGraphComponent subGraph = subGraphs.get(subGraphs.size() - 1);
			graphGroupNode.removeChild(subGraph);
			subGraphs.remove(subGraphs.size() - 1);

			// make sure that the number of xAxes isn't larger
			// than the number of subgraphs so this will prune
			// all unnecessary out

			for (int i = subGraphs.size(); i < xAxes.size(); i++)
				xAxes.remove(xAxes.size() - 1);

			// make sure that the number of yAxes isn't larger
			// than the number of subgraphs so this will prune
			// all unnecessary out

			for (int i = subGraphs.size(); i < yAxes.size(); i++)
				yAxes.remove(yAxes.size() - 1);
			
			numGraphs--;
		}
	}
	
	@Override
	protected void updateGraphs() {
		if (graph != null) {
			// check first if we have enough nodes if not we might have to
			// add a few more 	
			for (int i = subGraphs.size(); i < sets.size(); i++)
				buildAdditionalGraphNode();
			Iterator<IDataset> iter = sets.iterator();
			Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
			Iterator<AxisValues> xaxisIter = xAxes.iterator();
			Iterator<AxisValues> yaxisIter = yAxes.iterator(); 
			Iterator<DefaultPointShader> shaderIter = graphPointShaders.iterator();
			while (iter.hasNext()) {
				IDataset dataSet = iter.next();
				SceneGraphComponent currentGraph = graphIter.next();		
				AxisValues xAxis = xaxisIter.next();
				AxisValues yAxis = yaxisIter.next();
				currentGraph
						.setGeometry(createGraphGeometry(dataSet, xAxis,yAxis));
				DefaultPointShader dps = shaderIter.next();
				if (dataMin == dataMax)
					dps.setPointSize(dataMin);
				else
					dps.setPointSize(1.0);
					
				updateErrorBarNodesToPointNode(xAxis, yAxis);
			}
			// now set the geometry to the remaining graph nodes to null
			while (graphIter.hasNext()) {
				SceneGraphComponent currentGraph = graphIter.next();
				currentGraph.setGeometry(null);
			}
		}
	}	

	@Override
	protected void updateWindowWithNewRanges(List<IDataset> datasets, SelectedWindow window) {
		Iterator<AxisValues> xaxisIter = xAxes.iterator();
		Iterator<AxisValues> yaxisIter = yAxes.iterator();
		double realYmin = Float.MAX_VALUE;
		double realYmax = -Float.MAX_VALUE;
		double realXmin = Float.MAX_VALUE;
		double realXmax = -Float.MAX_VALUE;
		while (xaxisIter.hasNext()) {
			AxisValues xAxis = xaxisIter.next();
			AxisValues yAxis = yaxisIter.next();
			realXmin = Math.min(realXmin, xAxis.getMinValue());
			realXmax = Math.max(realXmax, xAxis.getMaxValue());
			realYmin = Math.min(realYmin, yAxis.getMinValue());
			realYmax = Math.max(realYmax, yAxis.getMaxValue());
		}
		window.setStartWindowX(realXmin);
		window.setEndWindowX(realXmax);
		window.setStartWindowY(realYmin);
		window.setEndWindowY(realYmax);
	}	

	@Override
	public void updateAllGraphAppearances() {
		for (int i = 0; i < graphShaders.size(); i++) {
			Plot1DAppearance plotApp = graphColours.getLegendEntry(i);
			DefaultPointShader currentShader = graphPointShaders.get(i);
			plotApp.updateGraph(currentShader);
			SceneGraphComponent graph = subGraphs.get(i);
			graph.setVisible(plotApp.isVisible());
		}
	}
	
	@Override
	protected void refreshZoomedGraphsCustom(double startPosX, double endPosX) {
		globalRealXmax = endPosX;
		globalRealXmin = startPosX;
		Iterator<IDataset> iter = sets.iterator();
		Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
		Iterator<AxisValues> axisIter = xAxes.iterator();

		globalXmin = Float.MAX_VALUE;
		globalXmax = -Float.MAX_VALUE;

		while (iter.hasNext()) {
			IDataset currentSet = iter.next();
			AxisValues xAxis = axisIter.next();
			if (xAxis.getMaxValue() > globalRealXmin && xAxis.getMinValue() < globalRealXmax) {
				int startPosInData = 0;
				if (rangeZoom)
					startPosInData = xAxis.nearestUpEntry(startPosX);
				else
					startPosInData = xAxis.nearestLowEntry(startPosX);

				int endPosInData = xAxis.nearestUpEntry(endPosX);
				if (startPosInData == -1)
					startPosInData = 0;
				if (endPosInData == -1)
					endPosInData = currentSet.getShape()[0] - 1;
				globalXmin = Math.min(globalXmin, xAxis.getValue(startPosInData));
				globalXmax = Math.max(globalXmax, xAxis.getValue(endPosInData));
			} 
		}

		double oldXmax = globalRealXmax;
		double oldXmin = globalRealXmin;
		globalRealXmax = globalXmax;
		globalRealXmin = globalXmin;

		iter = sets.iterator();
		graphIter = subGraphs.iterator();
		axisIter = xAxes.iterator();
		Iterator<AxisValues> yAxisIter = yAxes.iterator();
		LinkedList<IDataset> tempBuffer = new LinkedList<IDataset>();
		LinkedList<AxisValues> tempAxisVals = new LinkedList<AxisValues>();
		LinkedList<AxisValues> tempYAxisVals = new LinkedList<AxisValues>();
		while (iter.hasNext()) {
			IDataset currentSet = iter.next();
			AxisValues xAxis = axisIter.next();
			AxisValues yAxis = yAxisIter.next();
			if (xAxis.getMaxValue() > oldXmin && xAxis.getMinValue() < oldXmax) {
				int startPosInData = 0;
				if (rangeZoom)
					startPosInData = xAxis.nearestUpEntry(startPosX);
				else
					startPosInData = xAxis.nearestLowEntry(startPosX);
				int endPosInData = xAxis.nearestUpEntry(endPosX);
				if (startPosInData == -1)
					startPosInData = 0;
				if (endPosInData == -1)
					endPosInData = currentSet.getShape()[0];
				else
					endPosInData = Math.min(endPosInData + 1, currentSet.getShape()[0]);
				IDataset zoomedDataSet = null;
				AxisValues zoomedYAxis = null;
				if (startPosInData < endPosInData) {
					zoomedDataSet = currentSet.getSlice(new int[] { startPosInData }, new int[] { endPosInData },
							new int[] { 1 });
					zoomedYAxis = yAxis.subset(startPosInData, endPosInData);
					
				}
				// protection against infinite zoom
				if (rangeZoom && zoomedDataSet != null) {
					if (zoomedDataSet.getSize() != 1) {
						dataMin = Math.min(dataMin, zoomedDataSet.min().doubleValue());
						dataMax = Math.max(dataMax, zoomedDataSet.max().doubleValue());
					} else {
						dataMin = Math.min(dataMin, zoomedDataSet.getDouble(0));
						dataMax = Math.max(dataMax, zoomedDataSet.getDouble(0) + 0.0001);
					}
				}
				AxisValues subXaxis = null;

				if (startPosInData < endPosInData) {
					subXaxis = xAxis.subset(startPosInData, endPosInData);
				}

				tempBuffer.add(zoomedDataSet);
				tempAxisVals.add(subXaxis);
				tempYAxisVals.add(zoomedYAxis);
			} else {
				tempBuffer.add(null);
				tempAxisVals.add(null);
				tempYAxisVals.add(null);
			}
		}

		buildTickLists();
		sanityCheckMinMax();
		// now do the actual plotting
		axisIter = tempAxisVals.iterator();
		yAxisIter = tempYAxisVals.iterator();
		iter = tempBuffer.iterator();
		while (graphIter.hasNext()) {
			SceneGraphComponent currentGraph = graphIter.next();
			IDataset currentSet = null;
			AxisValues subXaxis = null;
			AxisValues subYaxis = null;
			if (iter.hasNext()) {
				currentSet = iter.next();
				subXaxis = axisIter.next();
				subYaxis = yAxisIter.next();
			} 
			if (currentSet != null) {
				currentGraph.setGeometry(createGraphGeometry(currentSet, subXaxis,subYaxis));
			} else
				currentGraph.setGeometry(null);
		}
		tempAxisVals.clear();
		tempYAxisVals.clear();
		tempBuffer.clear();
	}
	
	@Override
	protected void updateGraphInZoom()
	{
		double startPosX = currentSelectWindow.getStartWindowX();
		double endPosX = currentSelectWindow.getEndWindowX();
		SelectedWindow bottom = undoSelectStack.get(undoSelectStack.size()-1);
		updateWindowWithNewRanges(sets,bottom);			
		refreshZoomedGraphsCustom(startPosX, endPosX);	
	}	
	

	@Override
	public void toggleErrorBars(boolean xcoord, boolean ycoord, boolean zcoord) {		
		xErrors.setVisible(xcoord);
		yErrors.setVisible(ycoord);
	}
	
}
