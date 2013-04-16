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

package org.dawnsci.plotting.jreality.tool;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.Rectangle3D;

/**
 * Mouse wheel zoom tool that scales the underlying scene nodes uniformly
 */

public class ClickWheelZoomTool extends AbstractTool {

	protected static final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
	protected static InputSlot worldToNDC = InputSlot.getDevice("WorldToNDC");
	protected SceneGraphComponent sceneNode;
	protected SceneGraphComponent translationNode;
	private double speed = 1.05;
	protected Rectangle3D rect = null;
	
	/**
	 * Constructor of the ClickWheelZoomTool 
	 * @param applyNode SceneGraph node the transformation should be applied to
	 */
	public ClickWheelZoomTool(SceneGraphComponent applyNode,
							  SceneGraphComponent transNode)	{
		super(InputSlot.getDevice("PrimaryUp"),
			  InputSlot.getDevice("PrimaryDown"));

		this.sceneNode = applyNode;
		this.translationNode = transNode;
		if (sceneNode.getTransformation() == null)
			sceneNode.setTransformation(new Transformation());
	}

	protected void updateEdgePoints(Matrix m) {
	}
	
	protected void updateEdgePointsAfterMouse(Matrix m) {
	}
	
	@Override
	public void activate(ToolContext tc) {
		DoubleArray tm = tc.getTransformationMatrix(pointerSlot);
		DoubleArray worldNDCtm = tc.getTransformationMatrix(worldToNDC);

		Matrix tempMat = new Matrix(tm);
		Matrix worldNDC = new Matrix(worldNDCtm);
		tempMat.multiplyOnLeft(worldNDC);
		double mousePosX = tempMat.getEntry(0,3) / tempMat.getEntry(3,3);
		double mousePosY = tempMat.getEntry(1,3) / tempMat.getEntry(3,3);
		double [] matrix = null;

		if (rect == null)
			rect = 
				BoundingBoxUtility.calculateChildrenBoundingBox(sceneNode);
				
		int wheel = 0;
		if (tc.getSource()== InputSlot.getDevice("PrimaryUp")) {
			wheel = 1;
		}
		else if (tc.getSource()== InputSlot.getDevice("PrimaryDown")) {
			wheel = -1;
		}
		
		if (wheel != 0) {
			matrix = sceneNode.getTransformation().getMatrix();
			double scaling = matrix[0];
			double transX = matrix[3];
			double transY = matrix[7];
			double transZ = matrix[11];
			double newScale = (wheel < 0 ? speed : 1.0 / speed);
				
			double minX = -rect.getExtent()[0] * 0.5;
			double minY = -rect.getExtent()[1] * 0.5;
			
			double maxX = rect.getExtent()[0] * 0.5;
			double maxY = rect.getExtent()[1] * 0.5;
			scaling *= newScale;				

			if (translationNode != null)
			{
				matrix = translationNode.getTransformation().getMatrix();
				minX -= matrix[3];
				minY -= matrix[7];
				maxX -= matrix[3];
				maxY -= matrix[7];				
			}
			mousePosX = minX + (mousePosX + 1) * 0.5 * (maxX - minX);
			mousePosY = minY + (mousePosY + 1) * 0.5 * (maxY - minY);
			Matrix M = MatrixBuilder.euclidean().translate(transX,transY,transZ).scale(scaling).getMatrix();
			MatrixBuilder.euclidean().translate(transX,transY,transZ).scale(scaling).assignTo(sceneNode);
			updateEdgePoints(M);
			
			if (translationNode != null)
			{
				matrix = translationNode.getTransformation().getMatrix();

				
				double tTransX = matrix[3];
				double tTransY = matrix[7];
				double tTransZ = matrix[11];
				tTransX -= mousePosX;
				tTransY -= mousePosY;
				tTransX += (mousePosX - (mousePosX * newScale - mousePosX));
				tTransY += (mousePosY - (mousePosY * newScale - mousePosY));
				Matrix M1 = MatrixBuilder.euclidean().translate(tTransX,tTransY,tTransZ).getMatrix();
				MatrixBuilder.euclidean().translate(tTransX,tTransY,tTransZ).assignTo(translationNode);
				updateEdgePointsAfterMouse(M1);
			}			
		}
		tc.getViewer().render();
	}	
	/**
	 * Get the zoom speed
	 * @return the zoom speed (default 1.05)
	 */
	public double getSpeed() {
		return speed;
	}
	
	/**
	 * Set the zoom speed
	 * @param speed set the new zoom speed
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}	
}
