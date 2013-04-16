/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
