/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.api.tool;

/**
 * Used to mark views which can contain tools
 * @author fcp94556
 *
 */
public interface IToolContainer {

	/**
	 * The active tool
	 * @return
	 */
	public IToolPage getActiveTool();
	
	/**
	 * Opens the tool in a dedicated view.
	 * @param tool
	 */
	public IToolPage createToolInDedicatedView(IToolPage tool) throws Exception;
}
