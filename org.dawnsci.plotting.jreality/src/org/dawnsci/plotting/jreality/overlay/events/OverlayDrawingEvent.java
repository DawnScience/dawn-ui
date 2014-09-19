/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.overlay.events;

import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayProvider;

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
