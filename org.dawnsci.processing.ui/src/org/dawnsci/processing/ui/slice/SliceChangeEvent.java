/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.slice;

import java.util.EventObject;

import org.eclipse.dawnsci.analysis.api.dataset.Slice;

public class SliceChangeEvent extends EventObject {

	private Slice[] slices;
	
	public SliceChangeEvent(Object source, Slice[] slice) {
		super(source);
	}
	
	public Slice[] getSlices() {
		return slices;
	}


}
