/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.overlay;


import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay1DProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.primitives.PrimitiveType;

/**
 *
 */
@SuppressWarnings("unused")
public class Demo1DOverlay implements Overlay1DConsumer {

	private Overlay1DProvider provider = null;
	private boolean drawing = false;
    private int primID = -1;
    private int primID2 = -1;
	private double sx,sy,ex,ey;
	
	/**
	 * Default constructor for the Demo1DOverlay
	 */
	
	public Demo1DOverlay()
	{
		sx = sy = ex = ey = 0.0;
	}
	
	@Override
	public void registerProvider(OverlayProvider provider) {
		this.provider = (Overlay1DProvider) provider;
		primID = provider.registerPrimitive(PrimitiveType.BOX);
		primID2 = provider.registerPrimitive(PrimitiveType.LINE);
		drawOverlay();
	}

	@Override
	public void unregisterProvider() {
		provider = null;

	}

	private void drawOverlay()
	{
		provider.begin(OverlayType.VECTOR2D);
		provider.setColour(primID, java.awt.Color.GRAY);
		provider.setTransparency(primID, 0.5);
		//provider.drawLine(primID, sx, sy, ex, ey);
		//provider.drawBox(primID, sx, sy, ex, ey);
		provider.drawBox(primID, 15.0, 0.2, 40.0, 0.8);
		provider.setColour(primID2, java.awt.Color.BLUE);
		provider.drawLine(primID2, 50,0.1,150,0.9);
		provider.end(OverlayType.VECTOR2D);
	}
	
	@Override
	public void areaSelected(AreaSelectEvent event) {
		if (event.getMode() == 0)
		{
		//	if (primID == -1 && provider != null)
				//primID = provider.registerPrimitive(PrimitiveType.LINE);
		//		primID = provider.registerPrimitive(PrimitiveType.BOX);
			sx = event.getPosition()[0];
			sy = event.getPosition()[1];
		}
		if (event.getMode() == 1)
		{
			ex = event.getPosition()[0];
			ey = event.getPosition()[1];
		//	drawOverlay();
		}
	}

	@Override
	public void removePrimitives() {
		primID = -1;
		primID2 = -1;
	}

}
