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

import org.dawnsci.plotting.jreality.overlay.VectorOverlayStyles;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;

/**
 * Super class for all types of OverlayPrimitives
 */
public abstract class OverlayPrimitive {

	protected SceneGraphComponent comp;
	protected java.awt.Color colour;
	protected boolean needToUpdateApp;
	protected boolean needToUpdateGeom;
	protected boolean needToUpdateTrans;
	protected boolean isHidden = false;
	protected boolean isFixedSize = false;
	protected Matrix transformMatrix;
	protected Matrix copyofTransformMatrix;
	protected Appearance ap;
	private double[] translationVec = new double[3];
	private double[] invTranslationVec = new double[3];
	private double[] ankorPoints = new double[2];
	
	/**
	 * Constructor of an OverlayPrimitive
	 * @param comp the underlying jReality SceneGraphComponent node this primitive
	 * is associated with
	 */
	public OverlayPrimitive(SceneGraphComponent comp)
	{
		this(comp,false);
	}
	
	/**
	 * Constructor of an OverlayPrimitive
	 * @param comp the underlying jReality SceneGraphComponent node this primitive 
	 * @param fixedSize set the sizing to be fixed (invariant to zooming) true or false
	 */
	public OverlayPrimitive(SceneGraphComponent comp, boolean fixedSize)
	{
		this.comp = comp;
		this.isFixedSize = fixedSize;
		needToUpdateApp = false;
		needToUpdateGeom = false;
		needToUpdateTrans = false;
		transformMatrix = null;
	}
	
	/**
	 * Set a new colour for the primitive
	 * @param newColour
	 */
	public void setColour(java.awt.Color newColour)
	{
		this.colour = newColour;
		needToUpdateApp = true;
	}
	
	/**
	 * translate the primitive
	 * @param translation x/y coordinate pair 
	 */
	public void translate(double[] translation)
	{
		needToUpdateTrans = true;
		translationVec[0] = translation[0];
		translationVec[1] = translation[1];
		translationVec[2] = 0.0;
		if (transformMatrix == null)
			transformMatrix = MatrixBuilder.euclidean().translate(translationVec).getMatrix();
		else
			transformMatrix = MatrixBuilder.euclidean(transformMatrix).translate(translationVec).getMatrix();
		copyofTransformMatrix = MatrixBuilder.euclidean(transformMatrix).getMatrix();
	}
	
	/**
	 * rotate the primitive
	 * @param angle rotation angle
	 * @param rotationCenter rotation center for the rotation
	 */
	public void rotate(double angle, double[] rotationCenter)
	{
		needToUpdateTrans = true;
		translationVec[0] = -rotationCenter[0];
		translationVec[1] = -rotationCenter[1];
		translationVec[2] = 0.0;
		invTranslationVec[0] = rotationCenter[0];
		invTranslationVec[1] = rotationCenter[1];
		invTranslationVec[2] = 0.0;
		if (transformMatrix == null) {
			transformMatrix = MatrixBuilder.euclidean().translate(invTranslationVec).rotateZ(angle).translate(translationVec).getMatrix();
		} else {
			transformMatrix = MatrixBuilder.euclidean(transformMatrix).translate(translationVec).rotateZ(angle).translate(invTranslationVec).getMatrix();
		}
		copyofTransformMatrix = MatrixBuilder.euclidean(transformMatrix).getMatrix();
	}

	/**
	 * Update the underlying SceneGraphComponent node
	 */
	public abstract void updateNode();
	
	/**
	 * Set the style of the overlay primitive
	 * @param style new style to be set
	 */
	public abstract void setStyle(VectorOverlayStyles style);
	
	/**
	 * Set the outline colour of the overlay primitive
	 * @param outlineColour colour of the outline
	 */
	public abstract void setOutlineColour(java.awt.Color outlineColour);
	
	
	/**
	 * Set the line thickness of the overlay primitive 
	 * @param thickness new thickness that should be set
	 */
	public abstract void setLineThickness(double thickness);
	
	
	/**
	 * Get the underlying SceneGraphComponent node
	 * @return the underlying SceneGraphComponent node
	 */
	public SceneGraphComponent getNode()
	{
		return comp;
	}
	
	/**
	 * Set transparency of the primitive
	 * @param value transparency value
	 */
	public abstract void setTransparency(double value);
		
	/**
	 * Set transparency of the outline primitive 
	 * @param value transparency value
	 */
	public abstract void setOutlineTransparency(double value);

	/**
	 * Is the primitive hidden or not
	 * @return if the primitive is hidden (true) otherwise (false)
	 */
	public boolean isHidden() {
		return isHidden; 
	}

	/**
	 * Hide the primitive
	 */
	public void hide() {
		isHidden = true;
	}
	
	/**
	 * Unhide the primitive
	 */
	
	public void unhide() {
		isHidden = false;
	}
	
	/**
	 * Is the primitive fixed in sizing (invariant to zooming)?
	 * @return true if yes otherwise false
	 */
	public boolean isFixedSize() {
		return isFixedSize;
	}

	/**
	 * Set the anchor point
	 * @param x
	 * @param y
	 */
	public void setAnchorPoint(double x, double y)
	{
		ankorPoints[0] = x;
		ankorPoints[1] = y;
	}
	
	/**
	 * Get the anchor points when fixed size has been specified
	 * @return anchor points
	 */
	public double[] getAnchorPoint() {
		return ankorPoints;
	}
	
	/**
	 * Get the internal primitive transformation matrix
	 * @return a copy of the internal primitive transformation matrix
	 */
	public Matrix getTransformationMatrix() {
		return copyofTransformMatrix;
	}

}
