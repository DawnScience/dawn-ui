/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components;

import org.dawnsci.common.richbeans.event.ValueAdapter;

/**
 * Used to mark value listeners listening for the purpose of
 * updating bounds. All listeners doing this job should extend
 * this class.
 */
public abstract class BoundsUpdater extends ValueAdapter{

	public BoundsUpdater(final String bounds, final String boundsKey) {
		super( bounds+" ("+boundsKey+")");
	}

}
