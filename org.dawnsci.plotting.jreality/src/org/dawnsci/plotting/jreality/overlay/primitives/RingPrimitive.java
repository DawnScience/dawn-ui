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
public class RingPrimitive extends OverlayPrimitive {


	private double [] coords;
	private int [][] faces;
	private int [][] edges;
	private DefaultLineShader dls = null;
	private DefaultGeometryShader dgs = null;
	private DefaultPolygonShader dps = null;
	private VectorOverlayStyles currentStyle = VectorOverlayStyles.FILLED;
    private static final int NUMOFSEGMENTS = 72;
    private Color outlineColour;
    private double lineThickness = 1.0;
    
	public RingPrimitive(SceneGraphComponent comp) {
		this(comp,false);
	}


	public RingPrimitive(SceneGraphComponent comp, boolean isFixed) {
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
		coords = new double[NUMOFSEGMENTS*2*3];
		faces = new int[NUMOFSEGMENTS*2-2][3];
		edges = new int[NUMOFSEGMENTS*2][2];		
	}
	
	
	
	@Override
	public void setLineThickness(double thickness) {
		lineThickness = thickness;
		needToUpdateApp = true;

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
	public void setStyle(VectorOverlayStyles style) {
		if (currentStyle != style) {
			needToUpdateApp = true;
			currentStyle = style;
		}
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

	
	/**
	 * Set the sector parameters
	 * @param cx centre point x coordinate
	 * @param cy centre point y coordinate
	 * @param outRadius outer radius
	 * @param innerRadius inner radius
	 */
	
	public void setRingParameters(double cx, double cy, double outRadius,
			                        double innerRadius) {
		double stepSize = 2.0 * Math.PI / (NUMOFSEGMENTS-1);
		for (int i = 0; i < NUMOFSEGMENTS; i++) {
			double angle =  i * stepSize;
			coords[i*3] = cx + innerRadius * Math.cos(angle);
			coords[i*3+1] = cy + innerRadius * Math.sin(angle);
			coords[i*3+2] = 0.0005;
			coords[(i+NUMOFSEGMENTS)*3] = cx + outRadius * Math.cos(angle);
			coords[(i+NUMOFSEGMENTS)*3+1] = cy + outRadius * Math.sin(angle);
			coords[(i+NUMOFSEGMENTS)*3+2] = 0.0005;
		}

		for (int i = 0; i < NUMOFSEGMENTS-1; i++) {
			faces[i*2][0] = i;
			faces[i*2][1] = i + NUMOFSEGMENTS;
			faces[i*2][2] = i + NUMOFSEGMENTS + 1;
			faces[i*2+1][0] = i;
			faces[i*2+1][1] = i + NUMOFSEGMENTS + 1;
			faces[i*2+1][2] = i + 1;
		}

		for (int i = 0; i < NUMOFSEGMENTS-1; i++) {
			edges[i][0] = i;
			edges[i][1] = i+1;
			edges[i+NUMOFSEGMENTS][0] = i + NUMOFSEGMENTS;
			edges[i+NUMOFSEGMENTS][1] = i + NUMOFSEGMENTS + 1;
		}

		needToUpdateGeom = true;
	}
	
	private IndexedFaceSet createRingGeometry() {
		IndexedFaceSetFactory factory = new IndexedFaceSetFactory();
		factory.setVertexCount(NUMOFSEGMENTS*2);
		factory.setFaceCount(NUMOFSEGMENTS*2-2);
		factory.setEdgeCount(NUMOFSEGMENTS*2);
		factory.setVertexCoordinates(coords);
		factory.setFaceIndices(faces);
		factory.setEdgeIndices(edges);
		factory.setGenerateFaceNormals(false);
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
			comp.setGeometry(createRingGeometry());
		}
		if (needToUpdateApp)
		{
			dls.setDiffuseColor(colour);
			dls.setLineWidth(lineThickness);
			dps.setDiffuseColor(colour);
			switch (currentStyle) {
				case FILLED:
				{
					dgs.setShowFaces(true);
					dgs.setShowLines(false);
					dgs.setShowPoints(false);
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

}
