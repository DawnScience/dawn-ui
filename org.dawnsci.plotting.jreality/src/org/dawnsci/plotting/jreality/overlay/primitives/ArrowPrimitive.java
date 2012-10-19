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
public class ArrowPrimitive extends OverlayPrimitive {

	private double coords[][] = new double[5][3];
	private double tempVec[] = new double[3];
	private IndexedLineSetFactory lineFactory = null;
	private int[][] edges = new int[3][2];
	private DefaultLineShader dls = null;
	private final double arrowSize = 0.5;
	private double lineThickness = 1.0;
	
	/**
	 * Constructor of an ArrowPrimitive
	 * @param comp SceneGraphComponent node the arrow belongs to
	 */
	public ArrowPrimitive(SceneGraphComponent comp) {
		this(comp,false);
	}

	/**
	 * Constructor of an ArrowPrimitive
	 * @param comp SceneGraphComponent node the arrow belongs to
	 * @param isFixed is the size fixed (invariant to zoom) true or false
	 */
	public ArrowPrimitive(SceneGraphComponent comp, boolean isFixed) {
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
		edges[1][0] = 2;
		edges[1][1] = 3;
		edges[2][0] = 2;
		edges[2][1] = 4;
	}

	/**
	 * Set the arrow points in world space
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 */
	
	public void setArrowPoints(double x, double y, double x1, double y1, double perc) {
		
		needToUpdateGeom = true;
		coords[0][0] = x;
		coords[0][1] = y;
		coords[0][2] = 0.0005;
		coords[1][0] = x1;
		coords[1][1] = y1;
		coords[1][2] = 0.0005;
		double deltaX = x1 - x;
		double deltaY = y1 - y;
		tempVec[0] = deltaX;
		tempVec[1] = deltaY;
		tempVec[2] = 0;
		double length = Math.max(0.00001, Math.sqrt(tempVec[0]*tempVec[0] + tempVec[1] * tempVec[1]));
		tempVec[0] /= length;
		tempVec[1] /= length;
		MatrixBuilder.euclidean().rotateZ(-1.25 * Math.PI).getMatrix().transformVector(tempVec);
		coords[2][0] = x + perc * deltaX;
		coords[2][1] = y + perc * deltaY;
		coords[2][2] = 0.0005;
		coords[3][0] = x + perc * deltaX + arrowSize * tempVec[0];
		coords[3][1] = y + perc * deltaY + arrowSize * tempVec[1];
		coords[3][2] = 0.0005;
		MatrixBuilder.euclidean().rotateZ(0.5 * Math.PI).getMatrix().transformVector(tempVec);

		coords[4][0] = x + perc * deltaX + arrowSize * tempVec[0];
		coords[4][1] = y + perc * deltaY + arrowSize * tempVec[1];
		coords[4][2] = 0.0005;
	}
	
	private IndexedLineSet createLineGeometry()
	{
		lineFactory.setVertexCount(5);
		lineFactory.setEdgeCount(3);
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
		//Nothing to do
	}

	@Override
	public void setLineThickness(double thickness) {
		lineThickness = thickness;
		needToUpdateApp = true;
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
