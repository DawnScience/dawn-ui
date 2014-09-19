/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.overlay;

import java.util.List;

import org.dawnsci.plotting.jreality.overlay.events.AbstractOverlayConsumer;
import org.dawnsci.plotting.jreality.overlay.events.OverlayDrawingEvent;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay1DProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.primitives.PrimitiveType;
import org.eclipse.swt.widgets.Display;

/**
 * Draws one or more vertical lines at various places in the data.
 */
public class LinesOverlay extends AbstractOverlayConsumer {

	private double[]         xValues;
	private double           min, max;
	private java.awt.Color[] colours;
	
	public LinesOverlay(Display display, final java.awt.Color[] colours) {
		super(display);
		this.colours = colours;
	}

	@Override
	protected int[] createDrawingParts(OverlayProvider provider) {
		final int[] lines = new int[colours.length];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = provider.registerPrimitive(PrimitiveType.LINE);
		}
		return lines;
	}
	

	@Override
	protected void drawOverlay(OverlayDrawingEvent evt) {
		draw();
	}
    private void draw() {
    	for (int i = 0; i < xValues.length; i++) {
            drawLine(xValues[i], colours[i], i);
    	}
   }

	private void drawLine(double x, java.awt.Color color, int partIndex) {
    	provider.begin(OverlayType.VECTOR2D);
    	provider.setColour(parts[partIndex], color);
    	((Overlay1DProvider)provider).drawLine(parts[partIndex], x, min, x, max);		
    	provider.end(OverlayType.VECTOR2D);
	}

    public void setXValues(final double[] xValues) {
    	this.xValues = xValues;
    }

	public void setY(final Dataset y) {
		this.min = y.min().doubleValue();
		this.max = y.min().doubleValue();
	}
	
	public void setYs(List<IDataset> ys) {
		this.min = Double.MAX_VALUE;
		this.max = -Double.MAX_VALUE;
		for (IDataset y : ys) {
			this.min = Math.min(min, y.min().doubleValue());
			this.max = Math.max(max, y.max().doubleValue());
		}
		
	}
}
