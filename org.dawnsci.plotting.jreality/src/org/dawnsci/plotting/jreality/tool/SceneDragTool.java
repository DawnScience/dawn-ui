/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.tool;

import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.DraggingTool;

/**
 *
 */
public class SceneDragTool extends DraggingTool {

	
	/**
	 * Default constructor of SceneDragTool
	 */
	public SceneDragTool()
	{
		super();
	}
	
	@Override
	public void perform(ToolContext tc) {
		super.perform(tc);
		tc.getViewer().render();
	}
}
