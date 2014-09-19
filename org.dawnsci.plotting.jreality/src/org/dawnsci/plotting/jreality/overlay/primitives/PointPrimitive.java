/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.overlay.primitives;

import java.awt.Color;

import org.eclipse.dawnsci.plotting.api.jreality.overlay.VectorOverlayStyles;

import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;

/**
 *
 */
public class PointPrimitive extends OverlayPrimitive {

	private double coords[][] = new double[1][3];
	private double pointSize = 1.0;
	protected PointSetFactory pFactory = null;
	private DefaultPointShader dps = null;
	
	public PointPrimitive(SceneGraphComponent comp) {
		this(comp,false);
	}
	
	public PointPrimitive(SceneGraphComponent comp, boolean fixedSize) {
		super(comp, fixedSize);
		ap = new Appearance();
		comp.setAppearance(ap);
		ap.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
		ap.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE,false);
		ap.setAttribute("useGLSL", false);

		DefaultGeometryShader dgs = 
			ShaderUtility.createDefaultGeometryShader(ap, true);
		 dps = 
			(DefaultPointShader)dgs.createPointShader("default");
		dps.setDiffuseColor(java.awt.Color.RED);
		dps.setPointSize(pointSize);
		dps.setSpheresDraw(false);
		colour = java.awt.Color.RED;
		dgs.setShowFaces(false);
		dgs.setShowLines(false);
		dgs.setShowPoints(true);	
		pFactory = new PointSetFactory();
		
	}

	protected PointSet createPointGeometry() {
		pFactory.setVertexCount(1);
		pFactory.setVertexCoordinates(coords);
		pFactory.update();
		return pFactory.getPointSet();
	}
	
	
	@Override
	public void setLineThickness(double thickness) {
		// Nothing to do
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
		if (value > 0.0f) {
			ap.setAttribute(CommonAttributes.POINT_SHADER+"." + CommonAttributes.TRANSPARENCY_ENABLED, true);
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		} else {
			ap.setAttribute(CommonAttributes.POINT_SHADER+"." + CommonAttributes.TRANSPARENCY_ENABLED, false);
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		}
		ap.setAttribute(CommonAttributes.POINT_SHADER+"." + CommonAttributes.TRANSPARENCY,value);
	}

	public void setPoint(double x, double y) {
		needToUpdateGeom = true;
		coords[0][0] = x;
		coords[0][1] = y;
		coords[0][2] = 0.005;		
	}

	public void setPhat(boolean phat) {
		needToUpdateApp = true;
		if (phat)
			pointSize = 5.0;
		else
			pointSize = 1.0;
	}
	@Override
	public void updateNode() {
		if (needToUpdateTrans) {
			MatrixBuilder.euclidean(transformMatrix).assignTo(comp);
		}
		if (needToUpdateGeom)
		{
			comp.setGeometry(createPointGeometry());
		}
		if (needToUpdateApp)
		{
			dps.setDiffuseColor(colour);
			dps.setPointSize(pointSize);
		}
		needToUpdateApp = false;
		needToUpdateGeom = false;
		needToUpdateTrans = false;
		transformMatrix = null;
	}

}
