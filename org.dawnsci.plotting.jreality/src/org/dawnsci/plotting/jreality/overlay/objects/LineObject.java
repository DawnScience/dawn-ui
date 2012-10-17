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

package org.dawnsci.plotting.jreality.overlay.objects;

import org.dawnsci.plotting.jreality.overlay.Overlay1DProvider;
import org.dawnsci.plotting.jreality.overlay.Overlay2DProvider;
import org.dawnsci.plotting.jreality.overlay.OverlayProvider;

/**
 *
 */
public class LineObject extends OverlayObject {

	private double sx,sy,ex,ey;
	
	public LineObject(int primID, OverlayProvider provider) {
		super(primID, provider);
	}
	
	public void setLinePoints(double sx,  double sy, double ex, double ey) {
		this.sx = sx;
		this.ex = ex;
		this.sy = sy;
		this.ey = ey;
	}
	
	public void setLineStart(double sx, double sy) {
		this.sx = sx;
		this.sy = sy;
	}
	
	public void setLineEnd(double ex, double ey) {
		this.ex = ex;
		this.ey = ey;
	}

	@Override
	public void draw() {
		if (provider instanceof Overlay1DProvider) {
			((Overlay1DProvider)provider).drawLine(primID, sx, sy, ex, ey);
		} else if (provider instanceof Overlay2DProvider)
			((Overlay2DProvider)provider).drawLine(primID, sx, sy, ex, ey);
	}
}
