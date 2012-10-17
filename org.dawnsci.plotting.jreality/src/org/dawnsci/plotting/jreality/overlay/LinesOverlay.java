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

package org.dawnsci.plotting.jreality.overlay;

import java.util.List;

import org.dawnsci.plotting.jreality.overlay.events.AbstractOverlayConsumer;
import org.dawnsci.plotting.jreality.overlay.events.OverlayDrawingEvent;
import org.dawnsci.plotting.jreality.overlay.primitives.PrimitiveType;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

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

	public void setY(final AbstractDataset y) {
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
