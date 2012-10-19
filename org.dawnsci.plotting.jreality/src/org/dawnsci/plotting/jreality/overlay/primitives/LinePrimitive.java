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

package org.dawnsci.plotting.jreality.overlay.primitives;

import java.awt.Color;

import org.dawnsci.plotting.jreality.overlay.VectorOverlayStyles;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.ShaderUtility;

/**
 *
 */
public class LinePrimitive extends OverlayPrimitive {

	private double coords[][] = new double[2][3];
	private double dataPoints[][] = new double[2][2];
	private IndexedLineSetFactory lineFactory = null;
	private int[][] edges = new int[1][2];
	private DefaultLineShader dls = null;
	private double lineThickness = 1.0;

	/**
	 * Constructor for a new line primitive
	 * @param comp
	 */
	public LinePrimitive(SceneGraphComponent comp) {
		this(comp,false);
	}

	/**
	 * Constructor for a new line primitive
	 * @param comp
	 * @param isFixed is the size fixed (invariant to zoom) true or false
	 */
	public LinePrimitive(SceneGraphComponent comp, boolean isFixed) {
		super(comp,isFixed);
		ap = new Appearance();
		comp.setAppearance(ap);
		ap.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
		ap.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("useGLSL", false);
		DefaultGeometryShader dgs = 
			ShaderUtility.createDefaultGeometryShader(ap, true);
		 dls = 
			(DefaultLineShader)dgs.createLineShader("default");
		dls.setDiffuseColor(java.awt.Color.WHITE);
		dgs.setShowFaces(false);
		dgs.setShowLines(true);
		dgs.setShowPoints(false);
		lineFactory = new IndexedLineSetFactory();
		edges[0][0] = 0;
		edges[0][1] = 1;
	}

	/**
	 * Set the line points in world space
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 */
	public void setLinePoints(double x, double y, double x1, double y1) {
		needToUpdateGeom = true;
		coords[0][0] = x;
		coords[0][1] = y;
		coords[0][2] = 0.0005;
		coords[1][0] = x1;
		coords[1][1] = y1;
		coords[1][2] = 0.0005;
	}
	
	/**
	 * Set the line coordinates in data coordinate space
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 */
	public void setDataPoints(double x, double y, double x1, double y1) {
		dataPoints[0][0] = x;
		dataPoints[0][1] = y;
		dataPoints[1][0] = x1;
		dataPoints[1][1] = y1;
	}
	
	/**
	 * Get the line coordinates in data coordinate space
	 * @return the line coordinates in data coordinate space
	 */
	public double[][] getDataPoints() {
		return dataPoints;
	}
	
	/**
	 * Get the line coordinates
	 * @return the line primitive coordinates
	 */
	public double[][] getLinePoints() {
		double [][] returnValues = {{coords[0][0], coords[0][1]},
									{coords[1][0], coords[1][1]}};
		return returnValues;
	}
	
	private IndexedLineSet createLineGeometry()
	{
		lineFactory.setVertexCount(2);
		lineFactory.setEdgeCount(1);
		lineFactory.setVertexCoordinates(coords);
		lineFactory.setEdgeIndices(edges);
		lineFactory.update();
		return lineFactory.getIndexedLineSet();
	}
	
	
	@Override
	public void updateNode() {
		if (needToUpdateTrans) {
			MatrixBuilder.euclidean(transformMatrix).assignTo(comp);
		}
		if (needToUpdateGeom)
		{
			comp.setGeometry(createLineGeometry());
		}
		if (needToUpdateApp)
		{
			dls.setDiffuseColor(colour);
			dls.setLineWidth(lineThickness);
		}
		needToUpdateApp = false;
		needToUpdateGeom = false;
		needToUpdateTrans = false;
		transformMatrix = null;
	}

	@Override
	public void setStyle(VectorOverlayStyles style) {
		// Nothing to do
	}

	@Override
	public void setOutlineColour(Color outlineColour) {
		// Nothing to do
	}

	@Override
	public void setLineThickness(double thickness) {
		needToUpdateApp = true;
		lineThickness = thickness;
		
	}

	@Override
	public void setOutlineTransparency(double value) {
		setTransparency(value);
	}

	@Override
	public void setTransparency(double value) {
		if (value > 0.0f) {
			ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY_ENABLED, true);
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		} else {
			ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY_ENABLED, false);
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		}
		ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY,value);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"." + CommonAttributes.TRANSPARENCY,value);
		ap.setAttribute(CommonAttributes.ADDITIVE_BLENDING_ENABLED,true);
	}

}
