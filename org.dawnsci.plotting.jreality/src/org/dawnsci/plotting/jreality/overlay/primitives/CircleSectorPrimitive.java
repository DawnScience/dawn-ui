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
public class CircleSectorPrimitive extends OverlayPrimitive {

	private double [][] coords;
	private int [][] faces;
	private int [][] edges;
	private DefaultLineShader dls = null;
	private DefaultGeometryShader dgs = null;
	private DefaultPolygonShader dps = null;
	private VectorOverlayStyles currentStyle = VectorOverlayStyles.FILLED;
    private Color outlineColour;
    private static final int MAXARCS = 37;
    private int numOfArcs = 2;
    private IndexedFaceSetFactory factory = null;
    private double lineThickness = 1.0;

    /**
	 * Constructor for a CircleSector primitive
	 * @param comp SceneGraphComponent node
	 */
	public CircleSectorPrimitive(SceneGraphComponent comp) {
		this(comp,false);
	}
    
    /**
	 * Constructor for a CircleSector primitive
	 * @param comp SceneGraphComponent node
     * @param isFixed is the size fixed (invariant to zoom) true or false
	 */
	public CircleSectorPrimitive(SceneGraphComponent comp, boolean isFixed) {
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
		coords = new double[MAXARCS*2][3];
		faces = new int[MAXARCS*2-2][3];
		edges = new int[MAXARCS*2][2];
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
	 * Set the sector parameters
	 * @param cx centre point x coordinate
	 * @param cy centre point y coordinate
	 * @param outRadius outer radius
	 * @param innerRadius inner radius
	 * @param startAngle starting angle in degrees
	 * @param endAngle ending angle in degrees
	 */
	
	public void setSectorParameters(double cx, double cy, double outRadius,
			                        double innerRadius, double startAngle, 
			                        double endAngle) {
		numOfArcs = (int)((endAngle-startAngle) / 5);
		if (numOfArcs < 4) numOfArcs = 4;
        if (numOfArcs > MAXARCS) numOfArcs = MAXARCS;
		double stepSize = (endAngle-startAngle) / (numOfArcs-1);
		for (int i = 0; i < numOfArcs; i++) {
			double angle =  Math.toRadians(startAngle + i * stepSize);
			coords[i][0] = cx + innerRadius * Math.cos(angle);
			coords[i][1] = cy + innerRadius * Math.sin(angle);
			coords[i][2] = 0.0005;
			coords[i+numOfArcs][0] = cx + outRadius * Math.cos(angle);
			coords[i+numOfArcs][1] = cy + outRadius * Math.sin(angle);
			coords[i+numOfArcs][2] = 0;
		}

		for (int i = 0; i < numOfArcs-1; i++) {
			faces[i*2][0] = i;
			faces[i*2][1] = i + numOfArcs;
			faces[i*2][2] = i + numOfArcs + 1;
			faces[i*2+1][0] = i;
			faces[i*2+1][1] = i + numOfArcs + 1;
			faces[i*2+1][2] = i + 1;
		}

		for (int i = 0; i < numOfArcs-1; i++) {
			edges[i][0] = i;
			edges[i][1] = i+1;
			edges[i+numOfArcs-1][0] = i + numOfArcs;
			edges[i+numOfArcs-1][1] = i + numOfArcs + 1;
		}
		edges[numOfArcs*2-2][0] = 0;
		edges[numOfArcs*2-2][1] = numOfArcs;
		edges[numOfArcs*2-1][0] = numOfArcs - 1;
		edges[numOfArcs*2-1][1] = numOfArcs*2 - 1;

		needToUpdateGeom = true;
	}
	
	private IndexedFaceSet createSectorGeometry() {
		factory.setVertexCount(numOfArcs*2);
		factory.setFaceCount(numOfArcs*2-2);
		factory.setEdgeCount(numOfArcs*2);
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
			comp.setGeometry(createSectorGeometry());
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
