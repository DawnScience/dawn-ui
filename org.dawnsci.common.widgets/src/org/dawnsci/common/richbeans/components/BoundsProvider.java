/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components;

import org.dawnsci.common.richbeans.event.ValueListener;

/**
 * Interface used to override bounds settings.
 * Used when input devices are bound to each other in value.
 * 
 * @author fcp94556
 *
 */
public interface BoundsProvider {
	/**
	 * The bound value
	 * @return double
	 */
    public double getBoundValue();
    
    /**
     * The acceptor of the BoundsProvider can also listen
     * to value changes from the BoundsProvider and update
     * it's bounds as required.
     * @param l
     */
    public void addValueListener(final ValueListener l);
}

	