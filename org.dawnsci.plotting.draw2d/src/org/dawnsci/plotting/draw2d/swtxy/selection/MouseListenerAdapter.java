/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;

public class MouseListenerAdapter implements MouseListener {

	private final org.eclipse.dawnsci.plotting.api.region.MouseListener deligate;
	public MouseListenerAdapter(org.eclipse.dawnsci.plotting.api.region.MouseListener l) {
		deligate = l;
	}
	public int hashCode() {
		return deligate.hashCode();
	}
	public boolean equals(Object object) {
		return deligate.equals(object);
	}

	@Override
	public void mousePressed(MouseEvent me) {
		deligate.mousePressed(new MouseEventAdapter(me));
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		deligate.mouseReleased(new MouseEventAdapter(me));
	}

	@Override
	public void mouseDoubleClicked(MouseEvent me) {
		deligate.mouseDoubleClicked(new MouseEventAdapter(me));
	}
	

}
