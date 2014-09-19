/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.dawnsci.plotting.api.region.MouseEvent;

public class MouseEventAdapter extends MouseEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7627603074904232662L;
	private final org.eclipse.draw2d.MouseEvent deligate;

	public MouseEventAdapter(org.eclipse.draw2d.MouseEvent source) {
		super(source.getSource());
		this.deligate = source;
	}

	@Override
	public int getButton() {
		return deligate.button;
	}

	@Override
	public int getX() {
		return deligate.x;
	}

	@Override
	public int getY() {
		return deligate.y;
	}

}
