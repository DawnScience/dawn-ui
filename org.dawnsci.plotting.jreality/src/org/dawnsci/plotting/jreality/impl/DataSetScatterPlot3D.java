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

import java.util.Iterator;
import java.util.List;

import org.dawnsci.plotting.jreality.core.AxisMode;
import org.dawnsci.plotting.jreality.data.ColourImageData;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.AbstractViewerApp;

/**
 *
 */
public class DataSetScatterPlot3D extends DataSet3DPlot3D {

	private static final double MAXRADII = 64.0;
	private double dataMin;
	private double dataMax;
	private boolean uniformSize;
	
	public DataSetScatterPlot3D(AbstractViewerApp app, 
			                    boolean useJOGL, 
			                    boolean useWindow) {
		super(app, useJOGL, useWindow);
		xAxis = AxisMode.CUSTOM;
		yAxis = AxisMode.CUSTOM;
		zAxis = AxisMode.CUSTOM;
	}
	
	@Override
	protected void buildDisplayDataSet() {
		displayData = currentData;
	}

	@Override
	protected void setGlobalMinMax() {
		if (!useWindow || windowEndPosX == 0)
		{
			globalRealXmin = xAxisValues.getMinValue();
			globalRealXmax = xAxisValues.getMaxValue();
			globalRealYmin = yAxisValues.getMinValue();
			globalRealYmax = yAxisValues.getMaxValue();
			globalZmin = zAxisValues.getMinValue();
			globalZmax = zAxisValues.getMaxValue();
			dataMin = displayData.min().doubleValue();
			dataMax = displayData.max().doubleValue();
		} else {
			// TODO Resolve this?
			
/*			globalRealXmin = xAxisValues.getValue(windowStartPosX);
			globalRealXmax = xAxisValues.getValue(windowEndPosX);
			globalRealYmin = yAxisValues.getValue(windowStartPosY);
			globalRealYmax = yAxisValues.getValue(windowEndPosY);*/		
		}
	}	

	@Override
	protected void determineGraphSize(int xAspect, int yAspect) {
		xSpan = MAXX;
		ySpan = MAXY;
	}

	private PointSet createGraphGeometry() {
		determineGraphSize(1, 1);
		int size = xAxisValues.size();
		double[] coords = new double[size * 3];
		double[] radii = new double[size];
		double[] colours = new double[size * 3];
		PointSetFactory factory = new PointSetFactory();
		Iterator<Double> xIter = xAxisValues.iterator();
		Iterator<Double> yIter = yAxisValues.iterator();
		Iterator<Double> zIter = zAxisValues.iterator();
		int counter = 0;
		while (xIter.hasNext()) {
			double xValue = xIter.next();
			double yValue = yIter.next();
			double zValue = zIter.next();
			coords[counter * 3] = -xSpan * 0.5 + xSpan * (xValue - globalRealXmin) / (globalRealXmax - globalRealXmin); 
			coords[counter * 3 + 1] = MAXZ * (zValue - globalZmin) / (globalZmax - globalZmin); 
			coords[counter * 3 + 2] = -ySpan + ySpan * (yValue - globalRealYmin) / (globalRealYmax - globalRealYmin);
			double dataEntry = displayData.getDouble(counter);
			if (!uniformSize) {
				if (dataMax > MAXRADII)
					radii[counter] = MAXRADII * dataEntry / dataMax;
				else
					radii[counter] = dataEntry;
			} else
				radii[counter] = 3.0f;
			if (colourTable == null) {
				colours[counter * 3] = (dataEntry - dataMin) / Math.max(1, (dataMax - dataMin));
				colours[counter * 3 + 1] = 0.25;
				colours[counter * 3 + 2] = 0.25;
			} else	if (hasJOGL) {
				int index = (int) (colourTable.getWidth() * ((dataEntry - colourTableMin) / (colourTableMax - colourTableMin)));
				index = Math.min(Math.max(0,index),colourTable.getWidth()-1);
				int packedRGBcolour = colourTable.get(index);
				int red = (packedRGBcolour >> 16) & 0xff;
				int green = (packedRGBcolour >> 8) & 0xff;
				int blue = (packedRGBcolour) & 0xff;							
				colours[counter * 3] = red / 255.0;
				colours[counter * 3 + 1] = green / 255.0;
				colours[counter * 3 + 2] = blue / 255.0;
			}
			counter++;
		}
		factory.setVertexCount(size);
		factory.setVertexCoordinates(coords);
		factory.setVertexColors(colours);
		factory.setVertexRelativeRadii(radii);
		factory.update();
		return factory.getPointSet();
	}

	@Override
	protected void updateDisplay(int xAspect, int yAspect) {
		setGlobalMinMax();
		graph.setGeometry(createGraphGeometry());
		if (xLabelNode != null) {
			xLabelNode.setGeometry(createXLabelsGeometry());
		}
		if (yLabelNode != null) {
			yLabelNode.setGeometry(createYLabelsGeometry());
		}
		if (zLabelNode != null) {
			zLabelNode.setGeometry(createZLabelsGeometry());
		}
		if (xTicksNode != null && xCoordActive) {
			xTicksNode.setGeometry(createXAxisTicksGeometry());
		}
		if (yTicksNode != null && yCoordActive) {
			yTicksNode.setGeometry(createYAxisTicksGeometry());
		}
		if (zTicksNode != null && zCoordActive) {
			zTicksNode.setGeometry(createZAxisTicksGeometry());
		}
	}
	
	@Override
	public SceneGraphComponent buildGraph(List<IDataset> datasets,
			SceneGraphComponent graph) {
		assert (datasets.size() > 0);
		if (graph != null)
		{
			this.graph = graph;
			currentData = datasets.get(0);
			buildDisplayDataSet();
			setGlobalMinMax();		
			graphAppearance = new Appearance();
			graph.setGeometry(createGraphGeometry());
			graph.setAppearance(graphAppearance);
			dgsGraph = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
			graphAppearance.setAttribute(CommonAttributes.VERTEX_COLORS_ENABLED, true);	
			graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
			graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
			graphAppearance.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.POINT_SIZE, 8.0);
			graphAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE,false);
			graphAppearance.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
			graphAppearance.setAttribute(CommonAttributes.TRANSPARENCY,0.5f);
			graphAppearance.setAttribute(CommonAttributes.Z_BUFFER_ENABLED,true);
			dgsGraph.setShowFaces(false);
			dgsGraph.setShowLines(false);
			dgsGraph.setShowPoints(true);
			DefaultPointShader dps = (DefaultPointShader)dgsGraph.createPointShader("default");
			dps.setPointSize(1.0);
		
			buildOtherNodes();
		}
		return graph;
	}

	@Override
	public void handleColourCast(ColourImageData colourTable,
			SceneGraphComponent graph, double minValue, double maxValue) {

		if (graph != null) {
			this.colourTable = colourTable;
			colourTableMin = minValue;
			colourTableMax = maxValue;
			if (graph.getGeometry() != null) {
				PointSet geom = (PointSet)graph.getGeometry();
				double [] colours = new double[displayData.getSize()*3];
				for (int i = 0; i < displayData.getSize(); i++) {
					double value = displayData.getDouble(i);
					int index = (int) (colourTable.getWidth() * ((value - minValue) / (maxValue - minValue)));
					index = Math.min(Math.max(0,index),colourTable.getWidth()-1);
					int packedRGBcolour = colourTable.get(index);
					int red = (packedRGBcolour >> 16) & 0xff;
					int green = (packedRGBcolour >> 8) & 0xff;
					int blue = (packedRGBcolour) & 0xff;							
					colours[i * 3] = red / 255.0;
					colours[i * 3 + 1] = green / 255.0;
					colours[i * 3 + 2] = blue / 255.0;
				}
				geom.setVertexAttributes(de.jreality.scene.data.Attribute.COLORS,
										 new de.jreality.scene.data.DoubleArrayArray.Inlined(colours,3));										
			}
		}
	}
	
	public void setTransparency(boolean useTransparency) {
		graphAppearance.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, useTransparency);
		graphAppearance.setAttribute(CommonAttributes.Z_BUFFER_ENABLED,!useTransparency);		
	}
	
	public void setDrawOutlinesOnly(boolean drawOutlines) {
		graphAppearance.setAttribute(CommonAttributes.SRC_ALPHA_BLEND_ONLY,drawOutlines);		
	}
	
	public void setUniformSize(boolean useUniform) {
		uniformSize = useUniform;
		graph.setGeometry(createGraphGeometry());
	}
}
