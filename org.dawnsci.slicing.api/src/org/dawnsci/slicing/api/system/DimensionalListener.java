/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.api.system;

import java.util.EventListener;

public interface DimensionalListener extends EventListener {

	/**
	 * notify the current slice dimensions.
	 * @param evt
	 */
	void dimensionsChanged(DimensionalEvent evt);
}
