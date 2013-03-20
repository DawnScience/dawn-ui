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

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.jreality.core.AxisMode;
import org.dawnsci.plotting.jreality.tick.Tick;
import org.dawnsci.plotting.jreality.tick.TickFormatting;
import org.dawnsci.plotting.jreality.util.ArrayPoolUtility;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.AbstractViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

/**
 *
 */
public class DataSet3DPlot1DStack extends DataSet3DPlot1D {

	private IndexedLineSetFactory areaSelectFactory;
	private SceneGraphComponent zTicks = null;
	private SceneGraphComponent zAxisLabel = null;
	private SceneGraphComponent zLabels = null;
	private SceneGraphComponent bBox = null;
	private DefaultTextShader dtsZAxisLabel;
	private DefaultTextShader dtsZTicks;
	private String zAxisLabelStr = null;
	private TickFormatting zLabelMode = TickFormatting.roundAndChopMode;
	private final static double TICKWIDTH = 0.25;
	private final static double INITROTATION = -0.25 * Math.PI;
	private double zOffset;
	private double zAxisLength;
	private double zGapBetweenGraphs = -0.0;

	private AxisValues zAxisValues = null;
	
	/**
	 * Constructor of the DataSet3DPlot1DStack
	 * @param app 
	 * @param colourTable
	 * @param hasJOGL 
	 */
	public DataSet3DPlot1DStack(AbstractViewerApp app, 
			   					Composite plotArea,
			   					Cursor defaultCursor,								
			   					Plot1DGraphTable colourTable, 
								boolean hasJOGL) {
		super(app, plotArea,defaultCursor,colourTable,hasJOGL);
		showXTicks = true;
		showYTicks = true;
		areaSelectFactory = new IndexedLineSetFactory();
	}
	
	@Override
	protected void repositionAndScaleAxis(float temp)
	{
		MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(
					-MAXX * 0.45, -MAXY * 0.5, -MAXY*0.33).scale(0.85).assignTo(axis);
	}
	
	@Override
	protected void resizeAndPositionNodes()
	{
		MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(-MAXX*0.45,-MAXY*0.5,-MAXY*0.33).scale(0.85).assignTo(graph);

		if (xTicks != null)
			MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(-MAXX * 0.45, -MAXY * 0.5, -MAXY*0.33).scale(0.85)
					.assignTo(xTicks);
		if (yTicks != null)
			MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(-MAXX * 0.45, -MAXY * 0.5, -MAXY*0.33).scale(0.85)
					.assignTo(yTicks);

	}
	
	private void updateGraphsZPosition() {
		Iterator<SceneGraphComponent> iter = subGraphs.iterator();
		Iterator<Double> axisIter = zAxisValues.iterator();
		double zGraphMin = zAxisValues.getMinValue();
		double zGraphMax = zAxisValues.getMaxValue();
		while (iter.hasNext()) {
			
			SceneGraphComponent graphComp = iter.next();
			if (axisIter.hasNext()) {
				double zValue = axisIter.next();
				double zPos = zAxisLength * (zValue - zGraphMin) / (zGraphMax - zGraphMin);
				MatrixBuilder.euclidean().translate(0.0f, 0.0f,	zPos).assignTo(graphComp);
			} else
				break;
		}		
	}
		
	@Override
	public SceneGraphComponent buildGraph(List<IDataset> datasets,
			SceneGraphComponent graph) {
		zGapBetweenGraphs = MAXY / datasets.size();
		if (zGapBetweenGraphs > 1.0) zGapBetweenGraphs = 1.0;
		zAxisLength = zGapBetweenGraphs * datasets.size();
		SceneGraphComponent returnGraph = super.buildGraph(datasets, graph);
		Camera sceneCamera = CameraUtility.getCamera(app.getCurrentViewer());
		sceneCamera.setFar(500.0);
		sceneCamera.setNear(0.5);
		graph.removeTool(tool);
		
		if (zAxisValues == null) {
			zAxisValues = new AxisValues(AbstractDataset.arange(1+datasets.size()+zOffset, AbstractDataset.FLOAT64));
		}
		
		if (zTicks != null)
			zTicks.setGeometry(createZTicksGeometry());
		if (zLabels != null)
			zLabels.setGeometry(createZLabelsGeometry());
				
		updateGraphsZPosition();
		return returnGraph;
	}
	
	@Override
	public void enableZoomTool(boolean enabled)
	{
		if (enabled && !zoomToolEnabled)
			graph.addTool(tool);
		else if (!enabled)
			graph.removeTool(tool);
		
		zoomToolEnabled = enabled;

	}

	@Override
	protected void determineRanges(List<IDataset> datasets) {
		Iterator<IDataset> iter = datasets.iterator();
		Iterator<Double> offIter = offsets.iterator();
		Iterator<AxisValues> axisIter = xAxes.iterator();
		globalYmin = Float.MAX_VALUE;
		globalYmax = -Float.MAX_VALUE;
		globalXmin = Float.MAX_VALUE;
		globalXmax = -Float.MAX_VALUE;
		globalRealXmin = Float.MAX_VALUE;
		globalRealXmax = -Float.MAX_VALUE;
		
		AxisValues xAxisValues = null;
		
		while (iter.hasNext()) {
			IDataset set = iter.next();
			globalYmin = Math.min(globalYmin, set.min().doubleValue());
			globalYmax = Math.max(globalYmax, set.max().doubleValue());
			globalXmax = Math.max(globalXmax, set.getShape()[0]);
			switch (xAxis) {
			case LINEAR:
				globalRealXmin = 0;
				globalRealXmax = globalXmax - 1;
				break;
			case LINEAR_WITH_OFFSET: {
				double offset = offIter.next();
				globalRealXmin = Math.min(offset, globalRealXmin);
				globalRealXmax = Math.max(globalXmax + offset, globalRealXmax) - 1;
			}
				break;
			case CUSTOM: {
				xAxisValues = axisIter.hasNext() ? axisIter.next() : xAxisValues;
				globalRealXmin = Math.min(globalRealXmin, xAxisValues.getMinValue());
				globalRealXmax = Math.max(globalRealXmax, xAxisValues.getMaxValue());
			}
				break;
			}
		}

		// check on yAxis is in offset mode if yes it will be added to the
		// minimum
		if (yAxis == AxisMode.LINEAR_WITH_OFFSET)
			globalYmin += yOffset;
		sanityCheckMinMax();
		// Now potential overwrite the global min/max depending on what
		// the tick labels come up with
		buildTickLists();

	}	
	@Override
	protected de.jreality.scene.Geometry createAreaSelection()
	{
		double[][] coords = ArrayPoolUtility.getDoubleArray(8);
		int[][] edges = ArrayPoolUtility.getIntArray(12);
		coords[0][0] = areaSelectStart[0];
        coords[0][1] = (rangeZoom ? 0.0 : areaSelectEnd[1]);
		coords[0][2] = 0.0;
		coords[1][0] = areaSelectEnd[0];
        coords[1][1] = (rangeZoom ? 0.0 : areaSelectEnd[1]);
		coords[1][2] = 0.0;
		coords[2][0] = areaSelectStart[0];
		coords[2][1] = (rangeZoom ? (MAXY-yInset) : areaSelectStart[1]);
		coords[2][2] = 0.0;
		coords[3][0] = areaSelectEnd[0];
		coords[3][1] = (rangeZoom ? (MAXY-yInset) : areaSelectStart[1]);
		coords[3][2] = 0.0;
		coords[4][0] = areaSelectStart[0];
        coords[4][1] = (rangeZoom ? 0.0 : areaSelectEnd[1]);
		coords[4][2] = zAxisLength;
		coords[5][0] = areaSelectEnd[0];
        coords[5][1] = (rangeZoom ? 0.0 : areaSelectEnd[1]);
		coords[5][2] = zAxisLength;
		coords[6][0] = areaSelectStart[0];
		coords[6][1] = (rangeZoom ? (MAXY-yInset) : areaSelectStart[1]);
		coords[6][2] = zAxisLength;
		coords[7][0] = areaSelectEnd[0];
		coords[7][1] = (rangeZoom ? (MAXY-yInset) : areaSelectStart[1]);
		coords[7][2] = zAxisLength;

		edges[0][0] = 0;
		edges[0][1] = 1;
		edges[1][0] = 0;
		edges[1][1] = 2;
		edges[2][0] = 2;
		edges[2][1] = 3;
		edges[3][0] = 1;
		edges[3][1] = 3;
		
		edges[4][0] = 4;
		edges[4][1] = 5;
		edges[5][0] = 4;
		edges[5][1] = 6;
		edges[6][0] = 6;
		edges[6][1] = 7;
		edges[7][0] = 5;
		edges[7][1] = 7;

		edges[8][0] = 0;
		edges[8][1] = 4;
		edges[9][0] = 1;
		edges[9][1] = 5;
		edges[10][0] = 2;
		edges[10][1] = 6;
		edges[11][0] = 3;
		edges[11][1] = 7;
		
		areaSelectFactory.setVertexCount(8);
		areaSelectFactory.setEdgeCount(12);
		areaSelectFactory.setVertexCoordinates(coords);	
		areaSelectFactory.setEdgeIndices(edges);
		areaSelectFactory.update();			
		return areaSelectFactory.getIndexedLineSet();
	}
	
	@Override
	public void updateGraph(List<IDataset> datasets) {
		zGapBetweenGraphs = MAXY / datasets.size();
		if (zGapBetweenGraphs  > 1.0) zGapBetweenGraphs = 1.0;
		zAxisLength = zGapBetweenGraphs * datasets.size();

		super.updateGraph(datasets);
		if (zAxisValues != null) {
			if (zAxisValues.size() < datasets.size()) {
				DoubleDataset values = DoubleDataset.arange(zOffset+1, zOffset + 1 + datasets.size(), 1);
				zAxisValues.addValues(values.getData());
			}
		} else {
			zAxisValues = new AxisValues(AbstractDataset.arange(1+datasets.size()+zOffset, AbstractDataset.FLOAT64));
		}
		updateGraphsZPosition();
		
		if (axis!=null) axis.setGeometry(createAxisGeometry());
		if (zAxisLabelStr != null)
			generateZLabelGeom();
		if (zLabels != null)
			zLabels.setGeometry(createZLabelsGeometry());
		if (zTicks != null)
			zTicks.setGeometry(createZTicksGeometry());		

	}
	
	@Override
	protected IndexedLineSet createAxisGeometry() {
		IndexedLineSetFactory factory  = new IndexedLineSetFactory();
		factory.setVertexCount(6);
		factory.setEdgeCount(3);
		double[][] axisCoords = ArrayPoolUtility.getDoubleArray(6);
		axisCoords[0][0] = 0;
		axisCoords[0][1] = 0;
		axisCoords[0][2] = 0;
		axisCoords[1][0] = (MAXX-xInset);
		axisCoords[1][1] = 0;
		axisCoords[1][2] = 0;
		axisCoords[2][0] = 0;
		axisCoords[2][1] = 0;
		axisCoords[2][2] = 0;
		axisCoords[3][0] = 0;
		axisCoords[3][1] = MAXY;
		axisCoords[3][2] = 0;
		axisCoords[4][0] = 0;
		axisCoords[4][1] = 0;
		axisCoords[4][2] = 0;
		axisCoords[5][0] = 0;
		axisCoords[5][1] = 0;
		axisCoords[5][2] = zAxisLength;
		int[][] axisEdges = ArrayPoolUtility.getIntArray(2);
		axisEdges[0][0] = 0;
		axisEdges[0][1] = 1;
		axisEdges[1][0] = 2;
		axisEdges[1][1] = 3;
		axisEdges[2][0] = 4;
		axisEdges[2][1] = 5;
		factory.setVertexCoordinates(axisCoords);
		factory.setEdgeIndices(axisEdges);
		factory.update();
		return factory.getIndexedLineSet();
	}

	@Override
	protected IndexedLineSet createXTicksGeometry() {
		double min = graphXmin;
		if (xLabels != null)
			xLabels.setGeometry(createXLabelsGeometry(xTicksLabels));
		if (showXTicks) 
		{
			IndexedLineSetFactory factory = new IndexedLineSetFactory();
			factory.setVertexCount(xTicksLabels.size() * 3);
			factory.setEdgeCount(xTicksLabels.size() * 2);
			double[][] coords = ArrayPoolUtility
					.getDoubleArray(xTicksLabels.size() * 3);
			int[][] edges = ArrayPoolUtility.getIntArray(xTicksLabels.size()*2);
			for (int i = 0; i < xTicksLabels.size(); i++) {
				double value = xTicksUnitSize * i;
				coords[i * 3][0] = (value / (graphXmax - min))
						* (MAXX - xInset);
				coords[i * 3][1] = -0.125;
				coords[i * 3][2] = -0.0001;
				coords[i * 3 + 1][0] = (value / (graphXmax - min))
						* (MAXX - xInset);
				coords[i * 3 + 1][1] = MAXY;
				coords[i * 3 + 1][2] = -0.0001;
				coords[i * 3 + 2][0] = (value / (graphXmax - min))
					* (MAXX - xInset);
				coords[i * 3 + 2][1] = -0.0001;
				coords[i * 3 + 2][2] = zAxisLength;
				edges[i*2][0] = i * 3;
				edges[i*2][1] = i * 3 + 1;
				edges[i*2+1][0] = i * 3;
				edges[i*2+1][1] = i*3 + 2;
			}
			factory.setVertexCoordinates(coords);
			factory.setEdgeIndices(edges);
			factory.update();
			return factory.getIndexedLineSet();
		}
		return null;
	}
	
	private PointSet createZLabelsGeometry()
	{
		tickFactory.setTickMode(zLabelMode);
		
		LinkedList<Tick> ticks = 
			tickFactory.generateTicks(Math.min(15, zAxisValues.size()), zAxisValues.getMinValue(), zAxisValues.getMaxValue(), (short) 2, false);
		PointSetFactory factory = new PointSetFactory();
		String[] edgeLabels = new String[ticks.size()];
        factory.setVertexCount(ticks.size());
        double [][] coords = ArrayPoolUtility.getDoubleArray(ticks.size());
        double range = zAxisValues.getMaxValue() - zAxisValues.getMinValue();
        for (int i = 0; i < ticks.size(); i++)
        {       	
 			coords[i][0] = -0.5;
			coords[i][1] = -0.25;
			if (i != 0 && i != ticks.size()-1) {
		       	Tick currentTick = ticks.get(i);
				coords[i][2] = zAxisLength * (currentTick.getTickValue() - zAxisValues.getMinValue()) / range;
				edgeLabels[i] = Double.toString(currentTick.getTickValue());
			} else if (i == 0){
				coords[i][2] = 0;
				edgeLabels[i] = Double.toString(zAxisValues.getMinValue());
			} else if (i == ticks.size()-1) {
				coords[i][2] = zAxisLength * (zAxisValues.getMaxValue() - zAxisValues.getMinValue()) / range;
				edgeLabels[i] = Double.toString(zAxisValues.getMaxValue());				
			}
        }
			
		factory.setVertexCoordinates(coords);
		factory.setVertexLabels(edgeLabels);		
		factory.update();
		return factory.getPointSet();
	}	
	
	private IndexedLineSet createZTicksGeometry()
	{
		tickFactory.setTickMode(zLabelMode);
		LinkedList<Tick> ticks = 
			tickFactory.generateTicks(Math.min(15, zAxisValues.size()), zAxisValues.getMinValue(), zAxisValues.getMaxValue(), (short) 2, false);

		IndexedLineSetFactory factory = new IndexedLineSetFactory();
        factory.setVertexCount(ticks.size() * 2);
        factory.setEdgeCount(ticks.size());
        double [][] coords = ArrayPoolUtility.getDoubleArray(ticks.size()*2);
		int[][] edges = ArrayPoolUtility.getIntArray(ticks.size());
        double range = zAxisValues.getMaxValue() - zAxisValues.getMinValue();

		for (int i = 0; i < ticks.size(); i++)
        {
			double zPos = zAxisLength * (ticks.get(i).getTickValue() - zAxisValues.getMinValue()) / range;
			if (i == 0)
				zPos = 0;
			else if (i == ticks.size() -1) {
				zPos = zAxisLength * (zAxisValues.getMaxValue() - zAxisValues.getMinValue()) / range; 
			}
			coords[i*2][0] = -TICKWIDTH;
			coords[i*2][1] = 0.0001;
			coords[i*2][2] = zPos;
			coords[i*2+1][0] = MAXX;
			coords[i*2+1][1] = 0.0001;
			coords[i*2+1][2] = zPos;
			edges[i][0] = i * 2;
			edges[i][1] = i * 2 + 1;
        }
		factory.setVertexCoordinates(coords);
		factory.setEdgeIndices(edges);
		factory.update();		
		return factory.getIndexedLineSet();
	}
	
	@Override
	public void buildZCoordLabeling(SceneGraphComponent zTicks) {
		this.zTicks = zTicks;
		if (zTicks != null) {
			zLabels = SceneGraphUtility.createFullSceneGraphComponent("zLabels");
			zTicks.addChild(zLabels);
			Appearance tickAppearance = new Appearance();
			zTicks.setAppearance(tickAppearance);
			DefaultGeometryShader dgs = 
				ShaderUtility.createDefaultGeometryShader(tickAppearance, true);
			tickAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
			DefaultLineShader dls = 
			         (DefaultLineShader)dgs.createLineShader("default");
			tickAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
					+ CommonAttributes.TUBES_DRAW, false);

			dls.setLineWidth(1.0);
			dls.setLineStipple(true);
			dls.setDiffuseColor(java.awt.Color.black);
			dgs.setShowFaces(false);
			dgs.setShowLines(true);
			dgs.setShowPoints(false);
			
			Appearance labelAppearance = new Appearance();
			zLabels.setAppearance(labelAppearance);
			DefaultGeometryShader dgsLabels =
				ShaderUtility.createDefaultGeometryShader(labelAppearance,true);
			labelAppearance.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
			labelAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
			labelAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);

			DefaultPointShader dps =
				     (DefaultPointShader)dgsLabels.createPointShader("default");
			dgsLabels.setShowFaces(false);
			dgsLabels.setShowLines(false);
			dgsLabels.setShowPoints(true);		
			dps.setPointSize(1.0);
			dps.setDiffuseColor(java.awt.Color.WHITE);
			dtsZTicks = (DefaultTextShader) dps.getTextShader();
			double[] offset = new double[]{0.0,0.0,0.0};
			dtsZTicks.setOffset(offset);
			dtsZTicks.setAlignment(javax.swing.SwingConstants.CENTER);
			dtsZTicks.setScale(FONT_SCALE);
			dtsZTicks.setDiffuseColor(java.awt.Color.black);
			dls.setLineWidth(1.0);
			dls.setLineStipple(true);
			dls.setDiffuseColor(java.awt.Color.black);
			dgs.setShowFaces(false);
			dgs.setShowLines(true);
			dgs.setShowPoints(false);
			java.awt.Dimension dim = app.getCurrentViewer().getViewingComponentSize();
			int width = dim.width;
			int height = dim.height;
			if (width == 0)
				width = FONT_SIZE_PIXELS_WIDTH;
			if (height == 0)
				height = FONT_SIZE_PIXELS_HEIGHT;

			MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(-MAXX*0.45,-MAXY*0.5,-MAXY*0.33).scale(0.85).assignTo(zTicks);	
		}
	}
	
	@Override
	public SceneGraphComponent buildCoordAxis(SceneGraphComponent axis) {
		super.buildCoordAxis(axis);
        zAxisLabel = SceneGraphUtility.createFullSceneGraphComponent("zAxisLabel");
        axis.addChild(zAxisLabel);
		Appearance zaxisLabelApp = new Appearance();
		zAxisLabel.setAppearance(zaxisLabelApp);
		zaxisLabelApp.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
		zaxisLabelApp.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false);
		zaxisLabelApp.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);

		DefaultGeometryShader dgsLabel =
			ShaderUtility.createDefaultGeometryShader(zaxisLabelApp, true);
		dgsLabel.setShowFaces(false);
		dgsLabel.setShowLines(false);
		dgsLabel.setShowPoints(true);
		DefaultPointShader dps =
			(DefaultPointShader)dgsLabel.createPointShader("default");
		dtsZAxisLabel = (DefaultTextShader)dps.getTextShader();
		dps.setPointSize(1.0);
		dps.setDiffuseColor(java.awt.Color.white);
		dtsZAxisLabel.setDiffuseColor(java.awt.Color.black);
		dtsZAxisLabel.setTextdirection(0);
		dtsZAxisLabel.setScale(FONT_AXIS_SCALE);
		return axis;
	}
	
	@Override
	public void cleanUpGraphNode() {
		super.cleanUpGraphNode();
		if (axis != null)
		{
			axis.removeChild(zAxisLabel);
		}
		if (zTicks != null) {
			zTicks.removeChild(zLabels);
			zTicks.setGeometry(null);
		}
	}
	
	private void generateZLabelGeom()
	{
		PointSetFactory factory = new PointSetFactory();
		factory.setVertexCount(1);
		double [][] coords = ArrayPoolUtility.getDoubleArray(1);
		String [] edgeLabels = new String[1];
		edgeLabels[0] = zAxisLabelStr;
		coords[0][0] = -0.75;
		coords[0][1] = -0.75;
		coords[0][2] = zAxisLength * 0.5;
		factory.setVertexCoordinates(coords);
		factory.setVertexLabels(edgeLabels);		
		factory.update();		
		PointSet set = factory.getPointSet();
		if (zAxisLabel!=null && set!=null) zAxisLabel.setGeometry(set);
	}
	
	@Override
	public void setZAxisLabel(String label)
	{
		if (zAxisLabelStr == null || !label.equals(zAxisLabelStr))
		{
			zAxisLabelStr = label;
			generateZLabelGeom();
		}
	}
	
	@Override
	public void setZAxisLabelMode(TickFormatting newFormat) {
		zLabelMode = newFormat;
	}
	
	@Override
	public void notifyComponentResize(int width, int height) {
		if (graph != null) {
			MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(
					-MAXX * 0.45, -MAXY * 0.5, -MAXY*0.33).scale(0.85).assignTo(graph);
			if (axis != null)
				MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(-MAXX * 0.45, -MAXY * 0.5, -MAXY*0.33)
						.scale(0.85).assignTo(axis);
			if (xTicks != null)
				MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(-MAXX * 0.45, -MAXY * 0.5, -MAXY*0.33)
						.scale(0.85).assignTo(xTicks);
			if (yTicks != null)
				MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(-MAXX * 0.45, -MAXY * 0.5, -MAXY*0.33)
	   					.scale(0.85).assignTo(yTicks);
		}
		if (bBox != null)
			MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(-MAXX * 0.45, -MAXY * 0.5, -MAXY*0.33)
				.scale(0.85).assignTo(bBox);
		MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(-MAXX*0.45,-MAXY*0.5,-MAXY*0.33).scale(0.85).assignTo(zTicks);
	}
	
	public double getZAxisLengthFactor() {
		return zAxisLength / MAXZ;
	}
	
	public void setZAxisLengthFactor(double factor) {
		zAxisLength = MAXZ * factor;
		updateGraphsZPosition();
		if (bBox != null)
			bBox.setGeometry(createBBoxGeometry());
		if (xTicks != null)
			xTicks.setGeometry(createXTicksGeometry());
		if (zTicks != null)
			zTicks.setGeometry(createZTicksGeometry());
		if (zLabels != null)
			zLabels.setGeometry(createZLabelsGeometry());	
		if (axis != null)
			axis.setGeometry(createAxisGeometry());
		if (zAxisLabelStr != null)
			generateZLabelGeom();		
	}
	
	@Override
	public void setZAxisOffset(double offset) {
		zOffset = offset;
	}
	
	@Override
	public void setZAxisValues(AxisValues axis) {
		zAxisValues = axis;
	}	

	private IndexedLineSet createBBoxGeometry() {
		IndexedLineSetFactory factory = new IndexedLineSetFactory();
		
		double [][] coords = new double[8][3];
		int [][] edges = new int[12][2];
		
		coords[0][0] = 0.0;
		coords[0][1] = MAXY;
		coords[0][2] = 0.0;
		
		coords[1][0] = MAXX;
		coords[1][1] = MAXY;
		coords[1][2] = 0.0;
		
		coords[2][0] = MAXX;
		coords[2][1] = 0.0;
		coords[2][2] = 0.0;
		
		coords[3][0] = 0.0;
		coords[3][1] = 0.0;
		coords[3][2] = 0.0;	

		coords[4][0] = 0.0;
		coords[4][1] = MAXY;
		coords[4][2] = zAxisLength;
		
		coords[5][0] = MAXX;
		coords[5][1] = MAXY;
		coords[5][2] = zAxisLength;
		
		coords[6][0] = MAXX;
		coords[6][1] = 0.0;
		coords[6][2] = zAxisLength;
		
		coords[7][0] = 0.0;
		coords[7][1] = 0.0;
		coords[7][2] = zAxisLength;			
		
		edges[0][0] = 0;
		edges[0][1] = 1;
		edges[1][0] = 1;
		edges[1][1] = 2;
		edges[2][0] = 2;
		edges[2][1] = 3;
		edges[3][0] = 3;
		edges[3][1] = 0;

		edges[4][0] = 4;
		edges[4][1] = 5;
		edges[5][0] = 5;
		edges[5][1] = 6;
		edges[6][0] = 6;
		edges[6][1] = 7;
		edges[7][0] = 7;
		edges[7][1] = 4;

		edges[8][0] = 0;
		edges[8][1] = 4;
		edges[9][0] = 1;
		edges[9][1] = 5;
		edges[10][0] = 2;
		edges[10][1] = 6;
		edges[11][0] = 3;
		edges[11][1] = 7;
	
		
		factory.setEdgeCount(12);
		factory.setVertexCount(8);
		
		factory.setVertexCoordinates(coords);
		factory.setEdgeIndices(edges);
		
		factory.update();
		return factory.getIndexedLineSet();
	}	
	
	@Override
	public SceneGraphComponent buildBoundingBox() {
		bBox = SceneGraphUtility.createFullSceneGraphComponent("BBox");
		Appearance app = new Appearance();
		bBox.setAppearance(app);
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(app, true);
		app.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
		de.jreality.shader.DefaultLineShader dls = (de.jreality.shader.DefaultLineShader) dgs.createLineShader("default");
		dls.setDiffuseColor(Color.GRAY);
		dgs.setShowFaces(false);
		dgs.setShowLines(true);
		dgs.setShowPoints(false);
		bBox.setGeometry(createBBoxGeometry());
		MatrixBuilder.euclidean().rotate(INITROTATION,-1,1,0).translate(-MAXX * 0.45, -MAXY * 0.5, -MAXY*0.33)
			.scale(0.85).assignTo(bBox);
		return bBox;
	}
	
}
