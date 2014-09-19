/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.tool;

import java.util.EventListener;

/**
 * AreaSelect listener that can listen into areaSelect events
 */
public interface AreaSelectListener extends EventListener {

	/**
	 * Area selection has started
	 * @param e AreaSelectEvent that contains the information 
	 *          necessary to handle this event
	 */
	public void areaSelectStart(AreaSelectEvent e);
	
	/**
	 * Area selection is dragged
	 * @param e AreaSelectEvent that contains the information
	 * 			necessary to handle this event
	 */
	public void areaSelectDragged(AreaSelectEvent e);
		
	/**
	 * Area selection is finished
	 * @param e AreaSelectEvent that contains the information
	 * 			necessary to handle this event
	 */
	public void areaSelectEnd(AreaSelectEvent e);
	
}
