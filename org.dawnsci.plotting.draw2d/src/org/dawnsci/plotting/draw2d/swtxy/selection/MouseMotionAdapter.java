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
import org.eclipse.draw2d.MouseMotionListener;

public class MouseMotionAdapter implements MouseMotionListener {

	
	private org.eclipse.dawnsci.plotting.api.region.MouseMotionListener deligate;
	public MouseMotionAdapter(org.eclipse.dawnsci.plotting.api.region.MouseMotionListener l) {
		this.deligate = l;
	}
	
	public int hashCode() {
		return deligate.hashCode();
	}
	public boolean equals(Object object) {
		return deligate.equals(object);
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		deligate.mouseDragged(new MouseEventAdapter(me));
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		deligate.mouseEntered(new MouseEventAdapter(me));
	}

	@Override
	public void mouseExited(MouseEvent me) {
		deligate.mouseExited(new MouseEventAdapter(me));
	}

	@Override
	public void mouseHover(MouseEvent me) {
		deligate.mouseHover(new MouseEventAdapter(me));
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		deligate.mouseMoved(new MouseEventAdapter(me));
	}

}
