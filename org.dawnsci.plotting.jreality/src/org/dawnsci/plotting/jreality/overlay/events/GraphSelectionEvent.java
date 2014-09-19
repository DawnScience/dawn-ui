/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.overlay.events;

import java.util.EventObject;

import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;

/**
 *
 */
public class GraphSelectionEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected AreaSelectEvent start,end;

	/**
	 * @param source
	 */
	public GraphSelectionEvent(Object source) {
		super(source);
	}
	/**
	 * @return Returns the start.
	 */
	public AreaSelectEvent getStart() {
		return start;
	}

	/**
	 * @param start The start to set.
	 */
	public void setStart(AreaSelectEvent start) {
		this.start = start;
	}

	/**
	 * @return Returns the end.
	 */
	public AreaSelectEvent getEnd() {
		return end;
	}

	/**
	 * @param end The end to set.
	 */
	public void setEnd(AreaSelectEvent end) {
		this.end = end;
	}

}
