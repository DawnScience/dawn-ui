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
import java.awt.Font;

import org.dawnsci.plotting.jreality.overlay.VectorOverlayStyles;
import org.dawnsci.plotting.jreality.overlay.enums.LabelOrientation;

import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;

/**
 *
 */
public class LabelPrimitive extends OverlayPrimitive {

	private DefaultPointShader dpsLabel;
	private DefaultTextShader dtsLabel;
	private String labelStr;
	private PointSetFactory factory;
	private Font currentFont;
	private int currentAllign;
	private int currentTextOrientation = 0;
	private double currentScale = 0.008;
	private double [][] coords = new double [1][3];
	private double [][] dataPoints = new double[1][2];
	
	public LabelPrimitive(SceneGraphComponent comp) {
		this(comp,false);
	}

	public LabelPrimitive(SceneGraphComponent comp, boolean isFixed) {
		super(comp,isFixed);
		ap = new Appearance();
		ap.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
		ap.setAttribute(CommonAttributes.TEXT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.red);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.TEXT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.red);
		ap.setAttribute("useGLSL", false);
		comp.setAppearance(ap);
		DefaultGeometryShader dgs = 
			ShaderUtility.createDefaultGeometryShader(ap, true);
		dpsLabel = (DefaultPointShader)dgs.getPointShader();
		
		dpsLabel.setDiffuseColor(java.awt.Color.WHITE);
		dpsLabel.setPointSize(0.0);
		dpsLabel.setPointRadius(0.0);
		dtsLabel = (DefaultTextShader)dpsLabel.getTextShader();		
		dtsLabel.setScale(currentScale);
		dtsLabel.setDiffuseColor(java.awt.Color.red);		
		currentFont = dtsLabel.getFont();
		currentAllign = dtsLabel.getAlignment();
		dgs.setShowFaces(false);
		dgs.setShowLines(false);
		dgs.setShowPoints(true);
		factory = new PointSetFactory();
	}
	
	@Override
	public void setLineThickness(double thickness) {
		// Nothing to do

	}

	@Override
	public void setColour(java.awt.Color newColour)
	{
		super.setColour(newColour);
		needToUpdateGeom = true;
	}
	
	@Override
	public void setOutlineColour(Color outlineColour) {
		// Nothing to do		
	}

	@Override
	public void setOutlineTransparency(double value) {
		// Nothing to do

	}

	@Override
	public void setStyle(VectorOverlayStyles style) {
		// Nothing to do

	}

	@Override
	public void setTransparency(double value) {
		// Nothing to do

	}
	
	public void setLabelFont(Font newFont)
	{
		needToUpdateGeom = true;
		currentFont = newFont;
	}
	
	public void setLabelDirection(LabelOrientation newOrient)
	{
		switch (newOrient) {
			case HORIZONTAL:
				currentTextOrientation = 0;
			break;
			case VERTICAL:
				currentTextOrientation = 1;
			break;
		}
		needToUpdateGeom = true;
	}

	/**
	 * Set the label coordinates in data coordinate space
	 * @param x
	 * @param y
	 */
	public void setDataPoints(double x, double y) {
		dataPoints[0][0] = x;
		dataPoints[0][1] = y;
	}

	/**
	 * Get the label coordinates in data coordinate space
	 * @return the label coordinates in data coordinate space
	 */
	public double[][] getDataPoints() {
		return dataPoints;
	}
	
	public void setLabelPosition(double lx, double ly)
	{
		needToUpdateGeom = true;
		coords[0][0] = lx;
		coords[0][1] = ly;
		coords[0][2] = 0.0005;
	}

	public void setLabelString(String newString) {
		if (!newString.equals(labelStr))
		{
			labelStr = newString;
			needToUpdateGeom = true;
		}
	}
	
	public void setLabelAlignment(int allignment) {
		currentAllign = allignment;
		needToUpdateGeom = true;
	}
	private PointSet createLabelGeometry()
	{
		String[] edgeLabels = new String[]{labelStr};
		factory.setVertexCount(1);
		factory.setVertexCoordinates(coords);
		factory.setVertexLabels(edgeLabels);
		factory.update();
		return factory.getPointSet();
	}
	
	@Override
	public void updateNode() {
		if (needToUpdateTrans) {
			MatrixBuilder.euclidean(transformMatrix).assignTo(comp);
		}
		if (needToUpdateGeom)
		{
			dtsLabel = (DefaultTextShader) dpsLabel.getTextShader();
			dtsLabel.setTextdirection(currentTextOrientation);			
			dtsLabel.setScale(currentScale);
			dtsLabel.setDiffuseColor(colour);			
			dtsLabel.setAlignment(currentAllign);
			dtsLabel.setFont(currentFont);	
			comp.setGeometry(createLabelGeometry());
		}
		needToUpdateApp = false;
		needToUpdateGeom = false;
		needToUpdateTrans = false;
		transformMatrix = null;	
	}

}
