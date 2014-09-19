/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * This class (CameraRotationTool) is largely based on and copied from the
 * RotationTool in de.jreality.tools.
 */
package org.dawnsci.plotting.jreality.tool;

import de.jreality.scene.tool.AbstractTool;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.Rectangle3D;

public class CameraRotationTool extends AbstractTool {
	static InputSlot activationSlot = InputSlot.getDevice("RotateActivation");
	static InputSlot evolutionSlot = InputSlot
			.getDevice("TrackballTransformation");
	static InputSlot camPath = InputSlot.getDevice("WorldToCamera");
	transient SceneGraphComponent comp;
	transient Matrix center = new Matrix();
	transient EffectiveAppearance eap;
	boolean fixOrigin = false;
	private boolean rotateOnPick = false;
	transient private int metric;

	transient Matrix result = new Matrix();
	transient Matrix evolution = new Matrix();

	private boolean moveChildren;

	private boolean updateCenter;

	
	/**
	 *  Constructor for the CameraRotationTool
	 * 
	 */
	public CameraRotationTool() {
		super(activationSlot);
		addCurrentSlot(evolutionSlot);
		addCurrentSlot(camPath);
	}

	@Override
	public void activate(ToolContext tc) {
		comp = (moveChildren ? tc.getRootToLocal() : tc
				.getRootToToolComponent()).getLastComponent();
		// stop possible animation
		// TODO is this legitimate? perhaps we should introduce a boolean
		// "forceNewTransformation"
		if (comp.getTransformation() == null)
			comp.setTransformation(new Transformation());
		if (!fixOrigin) {
			PickResult currentPick = tc.getCurrentPick();
			if (rotateOnPick && currentPick != null)
				center = getRotationPoint(tc);
			else
				center = getCenter(comp);
		}
		if (eap == null
				|| !EffectiveAppearance.matches(eap, tc
						.getRootToToolComponent())) {
			eap = EffectiveAppearance.create(tc.getRootToToolComponent());
		}
		metric = eap.getAttribute("metric", Pn.EUCLIDEAN);

	}

	private Matrix getCenter(SceneGraphComponent comp) {
		Matrix centerTranslation = new Matrix();
		Rectangle3D bb = BoundingBoxUtility.calculateChildrenBoundingBox(comp);
		// need to respect the metric here
		MatrixBuilder.init(null, metric).translate(bb.getCenter()).assignTo(
				centerTranslation);
		return centerTranslation;
	}

	private Matrix getRotationPoint(ToolContext tc) {
		PickResult currentPick = tc.getCurrentPick();
		double[] obj = currentPick.getObjectCoordinates();
		double[] pickMatr = currentPick.getPickPath().getMatrix(null);
		SceneGraphPath compPath = (moveChildren ? tc.getRootToLocal() : tc
				.getRootToToolComponent());
		double[] compMatrInv = compPath.getInverseMatrix(null);
		double[] matr = Rn.times(null, compMatrInv, pickMatr);
		double[] rotationPoint = Rn.matrixTimesVector(null, matr, obj);

		Matrix centerTranslation = new Matrix();
		MatrixBuilder.init(null, metric).translate(rotationPoint).assignTo(
				centerTranslation);
		return centerTranslation;
	}

	@Override
	public void perform(ToolContext tc) {
		Matrix object2avatar = new Matrix((moveChildren ? tc.getRootToLocal()
				: tc.getRootToToolComponent()).getInverseMatrix(null));
		if (Rn.isNan(object2avatar.getArray())) {
			return;
		}
		try {
			object2avatar.assignFrom(P3.extractOrientationMatrix(null,
					object2avatar.getArray(), P3.originP3, metric));
		} catch (Exception e) {
			MatrixBuilder.euclidean().assignTo(object2avatar); // set identity
																// matrix
		}
		evolution.assignFrom(tc.getTransformationMatrix(evolutionSlot));
		evolution.conjugateBy(object2avatar);
		if (!fixOrigin && updateCenter) {
			PickResult currentPick = tc.getCurrentPick();
			if (rotateOnPick && currentPick != null)
				center = getRotationPoint(tc);
			else
				center = getCenter(comp);
		}
		if (metric != Pn.EUCLIDEAN)
			P3.orthonormalizeMatrix(evolution.getArray(), evolution.getArray(),
					10E-8, metric);
		result.assignFrom(comp.getTransformation());
		if (!fixOrigin)
			result.multiplyOnRight(center);
		result.multiplyOnRight(evolution);
		if (!fixOrigin)
			result.multiplyOnRight(center.getInverse());
		if (Rn.isNan(result.getArray()))
			return;
		comp.getTransformation().setMatrix(result.getArray());
		tc.getViewer().render();
	}

	@Override
	public void deactivate(ToolContext tc) {
	}

	
}
