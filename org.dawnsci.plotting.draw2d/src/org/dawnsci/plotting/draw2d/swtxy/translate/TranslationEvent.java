/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy.translate;

import java.util.EventObject;

import org.eclipse.draw2d.geometry.Point;

public class TranslationEvent extends EventObject {


	/**
	 * 
	 */
	private static final long serialVersionUID = 8052227785122947448L;
	
	private Point start;
	private Point end;

	private boolean ctrl;

	public TranslationEvent(Object source) {
		super(source);
	}

	/**
	 * @param source
	 * @param control true if a control key was pressed 
	 */
	public TranslationEvent(Object source, boolean control) {
		super(source);
		this.ctrl = control;
	}
	public TranslationEvent(Object source, Point start, Point end) {
		super(source);
		this.start = start;
		this.end   = end;
	}

	/**
	 * 
	 * @return true if the mouse was stationary during the translate, false otherwise.
	 */
	public boolean mouseStationary() {
		if (start==null || end==null) return false;
		return start.equals(end);
	}

	/**
	 * @return true if a control key was pressed during the translate
	 */
	public boolean controlKeyPressed() {
		return ctrl;
	}
}
