/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.event;

import java.util.EventListener;

/**
 * Listener interface for being notified of value changes
 * @author fcp94556
 *
 */
public interface BoundsListener extends EventListener {

	/**
	 * Called when value is greater than bounds
	 * @param e
	 */
	public void valueGreater(BoundsEvent e);
	
	/**
	 * Called when value is less than bounds
	 * @param e
	 */
	public void valueLess(BoundsEvent e);
	
	/**
	 * Called when value is in bounds
	 * @param e
	 */
	public void valueLegal(BoundsEvent e);

}

	