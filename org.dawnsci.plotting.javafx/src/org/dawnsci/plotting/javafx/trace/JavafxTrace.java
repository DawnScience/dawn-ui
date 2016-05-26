/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.javafx.trace;

import org.dawnsci.plotting.javafx.SceneDisplayer;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;

import javafx.scene.Node;

/**
 * Class to handle the addition and removal of the traces. 
 * @author Joel Ogden
 *
 *
 * 
 */
public abstract class JavafxTrace extends Image3DTrace
{	

	// !! I want to remove this, but am not sure how.
	private SceneDisplayer scene;
	
	public JavafxTrace(IPlottingSystemViewer<?> plotter, String name, SceneDisplayer scene) {
		super(plotter, name);
		this.scene = scene;
	}

	/**
	 * Used for the scene to access which object from the trace to display<br>
	 * For example, a Line trace will return the Line Object contained within the trace.
	 * 
	 * @return The object to be displayed
	 */
	public abstract Node getNode();

	public void dispose() {
		// remove node from scene
		if (getNode() != null)
			scene.removeNode(getNode());
		super.dispose();
	}
}
