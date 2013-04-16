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

import org.dawnsci.plotting.jreality.overlay.OverlayProvider;
import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;

/**
 * A simple event to wrap data involved in user making a selection
 * on the graph.
 */
public class OverlayDrawingEvent extends GraphSelectionEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OverlayProvider provider;
	private int[]           parts;
	private boolean         isInitialDraw;

	/**
	 * @param provider
	 * @param parts
	 */
	protected OverlayDrawingEvent(OverlayProvider provider, int[] parts) {
		super(provider);
		this.provider = provider;
		this.parts    = parts;
		this.isInitialDraw=true;
	}

	/**
	 * @param provider
	 * @param start
	 * @param end
	 * @param parts
	 */
	protected OverlayDrawingEvent(OverlayProvider provider, 
			                      AreaSelectEvent start,
			                      AreaSelectEvent end, 
			                      int[]           parts) {
		super(provider);
		this.provider = provider;
		this.parts    = parts;
		this.start    = start;
		this.end      = end;
		this.isInitialDraw=false;
	}

	
	/**
	 * @return Returns the provider.
	 */
	public OverlayProvider getProvider() {
		return provider;
	}

	/**
	 * @param provider The provider to set.
	 */
	public void setProvider(OverlayProvider provider) {
		this.provider = provider;
	}

	/**
	 * @return Returns the parts.
	 */
	public int[] getParts() {
		return parts;
	}

	/**
	 * @param parts The parts to set.
	 */
	public void setParts(int[] parts) {
		this.parts = parts;
	}

	/**
	 * @return Returns the isInitialDraw.
	 */
	public boolean isInitialDraw() {
		return isInitialDraw;
	}

	/**
	 * @param isInitialDraw The isInitialDraw to set.
	 */
	public void setInitialDraw(boolean isInitialDraw) {
		this.isInitialDraw = isInitialDraw;
	}

}
