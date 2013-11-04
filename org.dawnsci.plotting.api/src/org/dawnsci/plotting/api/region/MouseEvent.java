/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.api.region;

import java.util.EventObject;

public abstract class MouseEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2140030391287691594L;

	public MouseEvent(Object source) {
		super(source);
	}

	public abstract int getButton();

	public abstract int getX();
	
	public abstract int getY();

}
