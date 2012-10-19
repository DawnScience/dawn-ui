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

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.jreality.core.ScaleType;
import org.dawnsci.plotting.jreality.data.ColourImageData;
import org.dawnsci.plotting.jreality.tick.Tick;
import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
import org.dawnsci.plotting.jreality.tool.AreaSelectEventListener;
import org.dawnsci.plotting.jreality.tool.SelectedWindow;
import org.dawnsci.plotting.jreality.util.ArrayPoolUtility;
import org.dawnsci.plotting.jreality.util.ScalingUtility;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.AbstractViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

/**
 *
 */
public class HistogramChartPlot1D extends DataSet3DPlot1D {

	private IDataset histoSet = null;
	private AxisValues axisValues = null;
	private SceneGraphComponent barGraph = null;
	private SceneGraphComponent colourTable = null;
	private Appearance colourTableApp = null;
	private boolean hasJOGL = false;
	private Texture2D tableTexture = null;
	private List<AreaSelectEventListener> selectionListeners =
		Collections.synchronizedList(new LinkedList<AreaSelectEventListener>());	
	private ScaleType xScaling = ScaleType.LINEAR;
	
	/**
	 * @param app
	 * @param colourTable
	 * @param hasJOGL 
	 */
	public HistogramChartPlot1D(AbstractViewerApp app, 
								Plot1DGraphTable colourTable,
								boolean hasJOGL) {
		super(app, null,null,colourTable,hasJOGL);
		this.hasJOGL = hasJOGL;
	}

	private IndexedFaceSet createColourTableGeom() {
		QuadMeshFactory factory = new QuadMeshFactory();
		double xValue = axisValues.getMaxValue();
		ScalingUtility.setSmallLogFlag((graphXmin < Math.E && graphXmin > 0.0));
		double logXMax = ScalingUtility.valueScaler(graphXmax, xScaling);
		double logXMin = ScalingUtility.valueScaler(graphXmin, xScaling);
        double logValue = ScalingUtility.valueScaler(xValue, xScaling);		
		double maxCoord = ((logValue - logXMin)) * ((MAXX - xInset) / (logXMax - logXMin));
		double[][][] coords = new double[2][2][3];
		coords[0][0][0] = 0.0;
		coords[0][0][1] = (MAXY-yInset)+0.9;
		coords[0][0][2] = 0.0;
		coords[0][1][0] = maxCoord;
		coords[0][1][1] = (MAXY-yInset)+0.9;
		coords[0][1][2] = 0.0;
		coords[1][0][0] = 0.0;
		coords[1][0][1] = (MAXY-yInset)+0.4;
		coords[1][0][2] = 0.0;
		coords[1][1][0] = maxCoord;
		coords[1][1][1] = (MAXY-yInset)+0.4;
		coords[1][1][2] = 0.0;
		factory.setVLineCount(2); // important: the v-direction is the left-most
									// index
		factory.setULineCount(2); // and the u-direction the next-left-most
									// index
		factory.setClosedInUDirection(false);
		factory.setClosedInVDirection(false);
		factory.setVertexCoordinates(coords);
		factory.setGenerateFaceNormals(true);
		factory.update();
		coords = null;
		return factory.getIndexedFaceSet();
	}
	
	private IndexedFaceSet createBarChart(IDataset histoSet)
	{
		QuadMeshFactory factory = new QuadMeshFactory();
		int graphSize = histoSet.getSize();
		Iterator<Double> valIter = axisValues.iterator();

		double [][][] coords = new double [2][(graphSize) * 2][3];
		double xValue = valIter.next();
		ScalingUtility.setSmallLogFlag((graphXmin < Math.E && graphXmin > 0.0));
		double logXMax = ScalingUtility.valueScaler(graphXmax, xScaling);
		double logXMin = ScalingUtility.valueScaler(graphXmin, xScaling);
		
		for (int x = 0; x < graphSize; x++)
		{
			double ySize = (MAXY-yInset) * histoSet.getDouble(x) / graphYmax;
			double logXValue = ScalingUtility.valueScaler(xValue, xScaling);
			coords[0][x*2][0] = ((logXValue - logXMin))
									* ((MAXX - xInset) / (logXMax - logXMin));
			coords[0][x*2][1] = ySize;
			coords[0][x*2][2] = 0.0;
			
			coords[1][x*2][0] = ((logXValue - logXMin))
									* ((MAXX - xInset) / (logXMax - logXMin));
			coords[1][x*2][1] = 0.0;
			coords[1][x*2][2] = 0.0;
		
			if (valIter.hasNext())
				xValue = valIter.next();

			logXValue = ScalingUtility.valueScaler(xValue, xScaling);
			
			coords[0][x*2+1][0] = ((logXValue - logXMin))
									* ((MAXX - xInset) / (logXMax - logXMin));
			coords[0][x*2+1][1] = ySize;
			coords[0][x*2+1][2] = 0.0;
		
			coords[1][x*2+1][0] = ((logXValue - logXMin))
									* ((MAXX - xInset) / (logXMax - logXMin));
			coords[1][x*2+1][1] = 0.0;
			coords[1][x*2+1][2] = 0.0;
		}
		factory.setVLineCount(2);		// important: the v-direction is the left-most index
		factory.setULineCount((graphSize)  * 2);		// and the u-direction the next-left-most index
		factory.setClosedInUDirection(false);	
		factory.setClosedInVDirection(false);	
		factory.setVertexCoordinates(coords);	
		factory.setGenerateFaceNormals(true);
		factory.update();
		coords = null;
		return factory.getIndexedFaceSet();	
	}
	
	@Override
	public SceneGraphComponent buildGraph(List<IDataset> datasets,
			SceneGraphComponent graph) {
		
		assert datasets.size() > 0;
		if (graph != null)
		{
			graphGroupNode = SceneGraphUtility
				.createFullSceneGraphComponent("groupNode");

			this.sets = datasets;
			this.graph = graph;
			determineRanges(datasets);
			currentSelectWindow = new SelectedWindow(0,(int)globalXmax,0,0);
			numGraphs = 0;
			// first build the line graphs representing the different colour channel graphs
			
			for (int i = 0; i < datasets.size()-1; i++)
			{
				IDataset currentDataSet = datasets.get(i);
				SceneGraphComponent subGraph =
					SceneGraphUtility.createFullSceneGraphComponent("graph.subGraph"+numGraphs);
				subGraphs.add(subGraph);
				subGraph.setGeometry(createGraphGeometry(currentDataSet,axisValues));
				Appearance graphAppearance = new Appearance();
				subGraph.setAppearance(graphAppearance);
				DefaultGeometryShader dgs = 
					ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
				graphAppearance.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, false);
				DefaultLineShader dls = 
					(DefaultLineShader)dgs.createLineShader("default");
				Plot1DAppearance plotApp = graphColours.getLegendEntry(numGraphs);
				plotApp.updateGraph(dls,dgs);
				graphLineShaders.add(dls);
				graphShaders.add(dgs);
			//	MatrixBuilder.euclidean().translate(0.0f,0.0f,Z_DIST_BETWEEN_GRAPH).assignTo(subGraph);
				graphGroupNode.addChild(subGraph);
				numGraphs++;
			}
			histoSet = datasets.get(datasets.size()-1);
			barGraph = 
				SceneGraphUtility.createFullSceneGraphComponent("graph.subGraph"+numGraphs);
			barGraph.setGeometry(createBarChart(histoSet));
			//barGraph.setGeometry(createGraphGeometry(histoSet));
			Appearance graphAppearance = new Appearance();
			barGraph.setAppearance(graphAppearance);
			DefaultGeometryShader dgs = 
				ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
			DefaultPolygonShader dps = 
				(DefaultPolygonShader)dgs.createPolygonShader("default");
			if (!hasJOGL)
				graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			dps.setDiffuseColor(Color.GRAY);
			dps.setSpecularColor(Color.BLACK);
			dgs.setShowFaces(true);
			dgs.setShowLines(false);
			dgs.setShowPoints(false);
			MatrixBuilder.euclidean().translate(0.0f,0.0f,0.0f).assignTo(barGraph);
			graphGroupNode.addChild(barGraph);
			numGraphs++;
			
			java.awt.Dimension dim = app.getCurrentViewer().getViewingComponentSize();
			float yRatio = (float)dim.width/(float)dim.height;
			if (yRatio > 1.0f)
			{
				MatrixBuilder.euclidean().scale(yRatio,1.0,1.0).translate(-MAXX*0.45,-MAXY*0.5,0.0).assignTo(graph);
				if (xTicks != null)
					MatrixBuilder.euclidean().scale(yRatio,1.0,1.0).translate(-MAXX*0.45,-MAXY*0.5,0.0).assignTo(xTicks);
				if (yTicks != null)
					MatrixBuilder.euclidean().scale(yRatio,1.0,1.0).translate(-MAXX*0.45,-MAXY*0.5,0.0).assignTo(yTicks);

			} else {
				MatrixBuilder.euclidean().scale(1.0,1.0/yRatio,1.0).translate(-MAXX*0.45,-MAXY*0.5,0.0).assignTo(graph);
				if (xTicks != null)
					MatrixBuilder.euclidean().scale(1.0,1.0/yRatio,1.0).translate(-MAXX*0.45,-MAXY*0.5,0.0).assignTo(xTicks);								
				if (yTicks != null)
					MatrixBuilder.euclidean().scale(1.0,1.0/yRatio,1.0).translate(-MAXX*0.45,-MAXY*0.5,0.0).assignTo(yTicks);												
			}
			Camera sceneCamera = CameraUtility.getCamera(app.getCurrentViewer());
	        sceneCamera.setFieldOfView(56.5f);
	        
	        // now add an invisible background component
	        // that we use for the picking so we can easily get
	        // object space coordinates for our mouse for the AreaSelection Tool
	        // make sure that this node gets removed from the graph node
	        // when DataSet3DPlot1D is no longer the active plotter
	        // and the tool as well
	        graph.addChild(graphGroupNode);
	        if (background == null)
	        {
	        	buildBackground();
	        	graph.addChild(background);
        		graph.addTool(tool);
	        }
	        if (colourTable == null)
	        {
				colourTable = SceneGraphUtility.createFullSceneGraphComponent("table");
				colourTable.setGeometry(createColourTableGeom());
				colourTableApp = new Appearance();
				colourTableApp.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
				colourTableApp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
				byte[] imageData = new byte[256 * 4];
				for (int i = 0; i < 256; i++) {
					imageData[i*4] = 127;
					imageData[i*4+3]=-127;
				}
				de.jreality.shader.ImageData texImg = new de.jreality.shader.ImageData(imageData, 256, 1);
				
				tableTexture = TextureUtility.createTexture(colourTableApp, POLYGON_SHADER, texImg);
				tableTexture.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
				tableTexture.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
				tableTexture.setMagFilter(Texture2D.GL_LINEAR);
				tableTexture.setMinFilter(Texture2D.GL_LINEAR);
				tableTexture.setMipmapMode(true);
				colourTable.setAppearance(colourTableApp);
				graph.addChild(colourTable);
	        }
	        if (areaSelection == null)
	        {
	        	buildAreaSelection();
	        }
	        
			if (xTicks != null)
				xTicks.setGeometry(createXTicksGeometry());
			if (yTicks != null)
				yTicks.setGeometry(createYTicksGeometry());	        
		}		
		return graph;
	}

	

	private void updateGraphs(boolean needHistoUpdate)
	{
		if (graph != null)
		{
			// update channel graphs
			for (int i = 0; i<sets.size()-1; i++)
			{
				IDataset dataSet = sets.get(i);
				SceneGraphComponent currentGraph = subGraphs.get(i);
				currentGraph.setGeometry(createGraphGeometry(dataSet,axisValues));						
			}
			
			if (needHistoUpdate)
			{
				histoSet = sets.get(sets.size()-1);
				barGraph.setGeometry(createBarChart(histoSet));
				if (colourTable != null)
					colourTable.setGeometry(createColourTableGeom());
			}
		}
	}

	@Override
	protected void zoomCustom(double startXArea, double endXArea, 
							  double startYArea, double endYArea )
	{
		double graphSize = globalRealXmax - globalRealXmin;
		double startPosX = (graphSize * startXArea/ (MAXX-xInset))+globalRealXmin;		
        double endPosX =   (graphSize * endXArea / (MAXX-xInset))+globalRealXmin;
        // add current window view into the undo stack and create a new one
        if (startPosX < axisValues.getMinValue())
        	startPosX = axisValues.getMinValue();
        if (undoSelectStack.size() == 0)
        {
        	currentSelectWindow.setStartWindowX(globalRealXmin);
        	currentSelectWindow.setEndWindowX(globalRealXmax);
        }
        undoSelectStack.add(0,currentSelectWindow);
        currentSelectWindow = new SelectedWindow(startPosX,endPosX,0,0);
		double[] dataPos = {startPosX,endPosX};
		notifyAreaSelect(dataPos);
	}
	
	@Override
	protected void determineRanges(List<IDataset> datasets)
	{
		globalYmin = Float.MAX_VALUE;
		globalYmax = -Float.MAX_VALUE;
		globalXmin = 0.0;
		globalXmax = -1.0;
		globalRealXmin = Float.MAX_VALUE;
		globalRealXmax = -Float.MAX_VALUE;
		for (IDataset d : datasets) {
			globalYmin = Math.min(globalYmin, d.min().doubleValue());
			globalYmax = Math.max(globalYmax, d.max().doubleValue());
			globalXmax = Math.max(globalXmax, d.getShape()[0]);
		}
		if (axisValues != null)
		{
			globalRealXmin = Math.min(globalRealXmin, axisValues.getMinValue());
			globalRealXmax = Math.max(globalRealXmax, axisValues.getMaxValue());
		} else {
			globalRealXmin = globalXmin;
			globalRealXmax = globalXmax-1;			
		}
		buildTickLists();
	}

	@Override
	protected IndexedFaceSet createBackground()
	{
		QuadMeshFactory factory = new QuadMeshFactory();
		double [][][] coords = new double [2][2][3];
		coords[0][0][0] = -.5;
        coords[0][0][1] = MAXY;
		coords[0][0][2] = 0.0;
		coords[0][1][0] = MAXX;
        coords[0][1][1] = MAXY;
		coords[0][1][2] = 0.0;
		coords[1][0][0] = -0.5;
        coords[1][0][1] = 0.0;
		coords[1][0][2] = 0.0;
		coords[1][1][0] = MAXX;
        coords[1][1][1] = 0.0;
		coords[1][1][2] = 0.0;
		factory.setVLineCount(2);		// important: the v-direction is the left-most index
		factory.setULineCount(2);		// and the u-direction the next-left-most index
		factory.setClosedInUDirection(false);	
		factory.setClosedInVDirection(false);	
		factory.setVertexCoordinates(coords);	
		factory.setGenerateFaceNormals(true);
		factory.update();
		coords = null;
		return factory.getIndexedFaceSet();			
	}
	
	private void notifyAreaSelect(double[] area)
	{
		Iterator<AreaSelectEventListener> iter = selectionListeners.iterator();
		AreaSelectEvent newEvent = new AreaSelectEvent(tool,area,(char)2,-1);
		while (iter.hasNext())
		{
			AreaSelectEventListener listener = iter.next();
			listener.areaSelected(newEvent);
		}		
	}
	
	/**
	 * Undo a zoom step
	 */
	@Override
	public void undoZoom()
	{
		if (undoSelectStack.size() > 0)
		{
			currentSelectWindow = undoSelectStack.get(0);
			undoSelectStack.remove(0);
			double[] dataPos = {currentSelectWindow.getStartWindowX(),currentSelectWindow.getEndWindowX()};
			notifyAreaSelect(dataPos);
		}
	}
	
	@Override
	public void updateGraph(List<IDataset> datasets) {
		boolean needUpdateHisto = false;
		if (!histoSet.equals(datasets.get(datasets.size()-1)))
			needUpdateHisto = true;
		
		sets = datasets;
		determineRanges(sets);
		//currentSelectWindow = new SelectedWindow(0,(int)globalXmax,0,0);
		updateGraphs(needUpdateHisto);
		if (xTicks != null)
			xTicks.setGeometry(createXTicksGeometry());
		if (yTicks != null)
			yTicks.setGeometry(createYTicksGeometry());
	}	

	@Override
	public void setXAxisValues(AxisValues axis, int numOfDataSets) {
		axisValues = axis;
	}
	
	/**
	 * Add an AreaSelectEventListener to the listener list
	 * @param listener another AreaSelectEventListener
	 */
	public void addAreaSelectEventListener(AreaSelectEventListener listener)
	{
		selectionListeners.add(listener);
	}
	
	/**
	 * Remove an AreaSelectEventListener from the listener list
	 * @param listener listener that should be removed
	 */
	public void removeAreaSelectEventListener(AreaSelectEventListener listener)
	{
		selectionListeners.remove(listener);
	}
	
	/**
	 * Clear the zoom history 
	 */
	public void clearZoom() {
		undoSelectStack.clear();
	}
	
	@Override
	public void handleColourCast(ColourImageData colourTable,
			 	   				 SceneGraphComponent graph,
			 	   				 double minValue,
			 	   				 double maxValue) {
		int width = colourTable.getWidth();
		int height = colourTable.getHeight();
		byte[] imageRGBAdata = new byte[width * height * 4];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {			
				int RGBAvalue = colourTable.get(x, y);
				byte alpha = (byte)((RGBAvalue >> 24) & 0xff);
				byte red = (byte) ((RGBAvalue >> 16) & 0xff);
				byte green = (byte) ((RGBAvalue >> 8) & 0xff);
				byte blue = (byte) ((RGBAvalue) & 0xff);
				imageRGBAdata[(x + y * width) * 4] = red;
				imageRGBAdata[(x + y * width) * 4 + 1] = green;
				imageRGBAdata[(x + y * width) * 4 + 2] = blue;
				imageRGBAdata[(x + y * width) * 4 + 3] = alpha;
			}
		}
	
		de.jreality.shader.ImageData texImg = new de.jreality.shader.ImageData(imageRGBAdata, width, height);	
		tableTexture = TextureUtility.createTexture(colourTableApp, POLYGON_SHADER, texImg);
		tableTexture.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
		tableTexture.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
		tableTexture.setMagFilter(Texture2D.GL_LINEAR);
		tableTexture.setMinFilter(Texture2D.GL_LINEAR);
		tableTexture.setMipmapMode(true);
	}

	@Override
	public void cleanUpGraphNode() {
		super.cleanUpGraphNode();
		if (graph != null && colourTable != null)
			graph.removeChild(colourTable);
	}
	
	
	@Override
	protected PointSet createXLabelsGeometry(LinkedList<Tick> ticks) {
		PointSetFactory factory = new PointSetFactory();
		String[] edgeLabels = new String[ticks.size()];
		factory.setVertexCount(ticks.size());
		double[][] coords = ArrayPoolUtility.getDoubleArray(ticks.size());
		ScalingUtility.setSmallLogFlag((graphXmin < Math.E && graphXmin > 0.0));
		double logXMax = ScalingUtility.valueScaler(graphXmax, xScaling);
		double logXMin = ScalingUtility.valueScaler(graphXmin, xScaling);
		double oldCoord = -10.0;
		for (int i = 0; i < ticks.size(); i++) {
			double value = xTicksUnitSize * i;
			Tick currentTick = ticks.get(i);
			double logValue = ScalingUtility.valueScaler(value, xScaling);
			coords[i][0] = (logValue / (logXMax - logXMin))
						* (MAXX - xInset);
			coords[i][1] = -0.275;
			coords[i][2] = 0.0;
			if (coords[i][0]-oldCoord > 1.0) {
				edgeLabels[i] = currentTick.getTickName();
				oldCoord = coords[i][0];
			} else
				edgeLabels[i] = "";
			
		}
		factory.setVertexCoordinates(coords);
		factory.setVertexLabels(edgeLabels);
		factory.update();
		return factory.getPointSet();
	}	
	
	@Override
	protected IndexedLineSet createXTicksGeometry() {
		if (xLabels != null)
			xLabels.setGeometry(createXLabelsGeometry(xTicksLabels));
		
		ScalingUtility.setSmallLogFlag((graphXmin < Math.E && graphXmin > 0.0));
		double logXMax = ScalingUtility.valueScaler(graphXmax, xScaling);
		double logXMin = ScalingUtility.valueScaler(graphXmin, xScaling);
		
		if (showXTicks) {
			IndexedLineSetFactory factory = new IndexedLineSetFactory();
			factory.setVertexCount(xTicksLabels.size() * 2);
			factory.setEdgeCount(xTicksLabels.size());
			double[][] coords = ArrayPoolUtility
					.getDoubleArray(xTicksLabels.size() * 2);
			int[][] edges = ArrayPoolUtility.getIntArray(xTicksLabels.size());
			for (int i = 0; i < xTicksLabels.size(); i++) {
				double value = xTicksUnitSize * i;
				double logValue = ScalingUtility.valueScaler(value, xScaling);
				coords[i * 2][0] = (logValue / (logXMax - logXMin))
						* (MAXX - xInset);
				coords[i * 2][1] = -0.125;
				coords[i * 2][2] = -0.0001;
				coords[i * 2 + 1][0] = (logValue / (logXMax - logXMin))
						* (MAXX - xInset);
				coords[i * 2 + 1][1] = (MAXY-yInset);
				coords[i * 2 + 1][2] = -0.0001;
				edges[i][0] = i * 2;
				edges[i][1] = i * 2 + 1;
			}
			factory.setVertexCoordinates(coords);
			factory.setEdgeIndices(edges);
			factory.update();
			return factory.getIndexedLineSet();
		}
		return null;
	}	
	
	@Override
	public void setScaling(ScaleType newScaling) {
		xScaling = newScaling;
		if (graph != null)
		{
			xTicks.setGeometry(createXTicksGeometry());
			barGraph.setGeometry(createBarChart(histoSet));	
			colourTable.setGeometry(createColourTableGeom());
			refresh();
		}
	}	
}

