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

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;

/**
 *
 */
public class TrianglePrimitive extends OverlayPrimitive {

	private double [][] coords;
	private int [][] faces;
	private int [][] edges;
	private DefaultLineShader dls = null;
	private DefaultGeometryShader dgs = null;
	private DefaultPolygonShader dps = null;
	private VectorOverlayStyles currentStyle = VectorOverlayStyles.FILLED;
    private Color outlineColour;
    private IndexedFaceSetFactory factory = null;
    private double lineThickness = 1.0;

    /**
     * Constructor of a TrianglePrimitive
	 * @param comp
	 */
	public TrianglePrimitive(SceneGraphComponent comp) {
		this(comp,false);
	}
	
    /**
     * Constructor of a TrianglePrimitive
	 * @param comp
     * @param isFixed is the size fixed (invariant to zoom) true or false
	 */
	
	public TrianglePrimitive(SceneGraphComponent comp, boolean isFixed) {
		super(comp,isFixed);
		ap = new Appearance();
		comp.setAppearance(ap);
		ap.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
		ap.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute("useGLSL", false);
		dgs = 
			ShaderUtility.createDefaultGeometryShader(ap, true);
		dls = 
			(DefaultLineShader)dgs.createLineShader("default");
		dls.setDiffuseColor(java.awt.Color.WHITE);
		dps = 
			(DefaultPolygonShader)dgs.createPolygonShader("default");
		dps.setDiffuseColor(java.awt.Color.WHITE);
		dgs.setShowFaces(true);
		dgs.setShowLines(false);
		dgs.setShowPoints(false);
		factory = new IndexedFaceSetFactory();
		coords = new double[3][3];
		faces = new int[1][3];
		edges = new int [3][2];
		faces[0][0] = 0;
		faces[0][1] = 1;
		faces[0][2] = 2;
		edges[0][0] = 0;
		edges[0][1] = 1;
		edges[1][0] = 1;
		edges[1][1] = 2;
		edges[2][0] = 2;
		edges[2][1] = 0;
		
	}

	@Override
	public void setOutlineColour(Color outlineColour) {
		if (this.outlineColour != outlineColour)
		{
			this.outlineColour = outlineColour;
			needToUpdateApp = true;
		}
	}
	

	@Override
	public void setStyle(VectorOverlayStyles style) {
		if (currentStyle != style) {
			needToUpdateApp = true;
			currentStyle = style;
		}
	}

	/**
	 * Set the coordinates of the triangle
	 * @param x1 x coordinate for the first point
	 * @param y1 y coordinate for the first point
	 * @param x2 x coordinate for the second point
	 * @param y2 y coordinate for the second point
	 * @param x3 x coordinate for the third point
	 * @param y3 y coordinate for the third point
	 */
	public void setTriangleCoords(double x1, double y1,
			                      double x2, double y2,
			                      double x3, double y3)
	{
		coords[0][0] = x1;
		coords[0][1] = y1;
		coords[0][2] = 0.0005;
		coords[1][0] = x2;
		coords[1][1] = y2;
		coords[1][2] = 0.0005;
		coords[2][0] = x3;
		coords[2][1] = y3;
		coords[2][2] = 0.0005;
		needToUpdateGeom = true;
	}
	
	private IndexedFaceSet createTriangleGeometry()
	{
		factory.setEdgeCount(3);
		factory.setFaceCount(1);
		factory.setVertexCount(3);
		factory.setVertexCoordinates(coords);
		factory.setEdgeIndices(edges);
		factory.setFaceIndices(faces);
		factory.update();
		return factory.getIndexedFaceSet();
	}
	
	@Override
	public void updateNode() {
		if (needToUpdateTrans) {
			MatrixBuilder.euclidean(transformMatrix).assignTo(comp);
		}
		if (needToUpdateGeom)
		{
			comp.setGeometry(createTriangleGeometry());
		}
		if (needToUpdateApp)
		{
			dls.setDiffuseColor(colour);
			dps.setDiffuseColor(colour);
			dls.setLineWidth(lineThickness);
			switch (currentStyle) {
				case FILLED:
				{
					dgs.setShowFaces(true);
					dgs.setShowLines(false);
				}
				break;
				case OUTLINE:
				{
					dgs.setShowFaces(false);
					dgs.setShowLines(true);
				}
				break;
				case FILLED_WITH_OUTLINE:
				{
					dgs.setShowFaces(true);
					dgs.setShowLines(true);
					dls.setDiffuseColor(outlineColour);
				}
				break;
			}
		}
		needToUpdateApp = false;
		needToUpdateGeom = false;
		needToUpdateTrans = false;
		transformMatrix = null;
	}

	@Override
	public void setLineThickness(double thickness) {
		lineThickness = thickness;
		needToUpdateApp = true;	
	}

	@Override
	public void setOutlineTransparency(double value) {
		if (value > 0.0f) {
			ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY_ENABLED, true);
		} else {
			ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY_ENABLED, false);
		}
		ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY,value);
		ap.setAttribute(CommonAttributes.ADDITIVE_BLENDING_ENABLED,true);
	}

	@Override
	public void setTransparency(double value) {
		if (value > 0.0f) {
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		} else {
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		}
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"." + CommonAttributes.TRANSPARENCY,value);
		ap.setAttribute(CommonAttributes.ADDITIVE_BLENDING_ENABLED,true);
	}	
}
