/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.workbench.ui.diffraction.table;

import java.util.EventListener;

/**
 * Event Listener to monitor a change (adding/removing images) in the DiffCalTable
 * 
 * @author wqk87977
 *
 */
public interface TableChangedListener extends EventListener {

	/**
	 * Method that is run every time a change in the table occurs.
	 * Changes can be adding or removing image(s).
	 * @param event
	 *         ADDED or REMOVED
	 */
	public void tableChanged(TableChangedEvent event);
}