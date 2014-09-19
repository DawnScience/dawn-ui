/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.javafx.trace;

import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
/**
 * 
 * @author nnb55016
 * The following class is taken from the Oracle tutorial of Rotations in JavaFX 
 * Only the name of the class is different
 * The source code can be found at http://docs.oracle.com/javafx/2/transformations/rotation.htm
 */
class SurfaceTransformations extends Group{
	public Translate translation  = new Translate();
	public Translate p  = new Translate();
    public Translate ip = new Translate();
	public Rotate xRotation = new Rotate();
	{xRotation.setAxis(Rotate.X_AXIS);}
	public Rotate yRotation = new Rotate();
	{yRotation.setAxis(Rotate.Y_AXIS);}
	public Rotate zRotation = new Rotate();
	{zRotation.setAxis(Rotate.Z_AXIS);}
	public Scale scaling = new Scale();
	
	public SurfaceTransformations(){
		super();

		getTransforms().addAll(translation, p, xRotation, yRotation, 
				zRotation, scaling, ip);
	}
}
