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

import de.jreality.geometry.QuadMeshFactory;
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
public class BoxPrimitive extends OverlayPrimitive {

	private double [][][] coords = new double [2][2][3];
	private double [][] dataPoints = new double[2][2];
	private DefaultLineShader dls = null;
	private DefaultGeometryShader dgs = null;
	private DefaultPolygonShader dps = null;
	private QuadMeshFactory factory;
	private VectorOverlayStyles currentStyle = VectorOverlayStyles.FILLED;
    private Color outlineColour;
	private double lineThickness = 1.0;

	/**
	 * Generate BoxPrimitive 
	 * @param comp SceneGraph node this primitive is associated to
	 */
	
	public BoxPrimitive(SceneGraphComponent comp) {
		this(comp,false);
	}
	
	/**
	 * Generate BoxPrimitive 
	 * @param comp SceneGraph node this primitive is associated to
	 * @param isFixed is the size fixed (invariant to zoom) true or false
	 */

	public BoxPrimitive(SceneGraphComponent comp, boolean isFixed) {
		super(comp,isFixed);
		ap = new Appearance();
		comp.setAppearance(ap);
		ap.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
		ap.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D, Appearance.DEFAULT);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D_1, Appearance.DEFAULT);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D_2, Appearance.DEFAULT);	
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
		factory = new QuadMeshFactory();
	}

	/**
	 * Set BoxPrimitive points
	 * @param upperLeftX upper left x coordinate of the box
	 * @param upperLeftY upper left y coordinate of the box
	 * @param lowerRightX lower right x coordinate of the box
	 * @param lowerRightY lower right y coordinate of the box
	 */
	
	public void setBoxPoints(double upperLeftX, double upperLeftY,
			                 double lowerRightX, double lowerRightY)
	{
		coords[0][0][0] = upperLeftX;
        coords[0][0][1] = lowerRightY;
		coords[0][0][2] = 0.0005;
		coords[0][1][0] = lowerRightX;
        coords[0][1][1] = lowerRightY;
		coords[0][1][2] = 0.0005;
		coords[1][0][0] = upperLeftX;
        coords[1][0][1] = upperLeftY;
		coords[1][0][2] = 0.0005;
		coords[1][1][0] = lowerRightX;
        coords[1][1][1] = upperLeftY;
		coords[1][1][2] = 0.0005;
		needToUpdateGeom = true;
	}
	
	/**
	 * Set the box upper left and lower right coordinates in data coordinate space
	 * @param upperLeftX upper left x coordinate of the box
	 * @param upperLeftY upper left y coordinate of the box
	 * @param lowerRightX right x coordinate of the box
	 * @param lowerRightY right y coordinate of the box
	 */
	public void setDataPoints(double upperLeftX, double upperLeftY,
							  double lowerRightX, double lowerRightY)
	{
		dataPoints[0][0] = upperLeftX;
		dataPoints[0][1] = upperLeftY;
		dataPoints[1][0] = lowerRightX;
		dataPoints[1][1] = lowerRightY;
	}

	/**
	 * Get the box upper left and lower right coordinates in data coordinate space
	 * @return the two points in data coordinate space [0][0] = upper left x,
	 *         [0][1] = upper left y, [1][0] = lower right x, [1][1] = lower right y
	 */
	
	public double[][] getDataPoints() {
		return dataPoints;
	}
	
	private IndexedFaceSet createBoxGeometry()
	{
		factory.setVLineCount(2);	// important: the v-direction is the left-most index
		factory.setULineCount(2);	// and the u-direction the next-left-most index
		factory.setClosedInUDirection(false);
		factory.setClosedInVDirection(false);
		factory.setVertexCoordinates(coords);
		factory.setGenerateFaceNormals(true);
		factory.setEdgeFromQuadMesh(true);
		factory.setGenerateEdgesFromFaces(true);
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
			comp.setGeometry(createBoxGeometry());
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
	public void setStyle(VectorOverlayStyles style) {
		if (currentStyle != style) {
			needToUpdateApp = true;
			currentStyle = style;
		}
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
	public void setLineThickness(double thickness) {
		needToUpdateApp = true;
		lineThickness = thickness;
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
