/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.api.region;

import java.util.EventObject;

/**
 * Event with source of the IRegion affected.
 * 
 * @author fcp94556
 *
 */
public class RegionEvent extends EventObject {

	public RegionEvent(Object source) {
		super(source);
	}
	
	public IRegion getRegion() {
		return (IRegion)getSource();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3121767937881041584L;

}
