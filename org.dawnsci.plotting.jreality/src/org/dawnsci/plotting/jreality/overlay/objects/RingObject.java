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

import org.dawnsci.plotting.jreality.overlay.Overlay2DProvider;
import org.dawnsci.plotting.jreality.overlay.OverlayProvider;

/**
 * This is pretty useless but Alun wanted it! Gwyndaf's comment on this was:
 * "What the hell is this?"
 */
public class RingObject extends OverlayObject {

	private double rx,ry;
	private double inRadius = 1.0, outRadius = 2.0;
	
	public RingObject(int primID, OverlayProvider provider) {
		super(primID, provider);
	}

	public void setRingPosition(double rx, double ry) {
		this.rx = rx;
		this.ry = ry;
	}
	
	public void setRingRadii(double innerRadius, double outerRadius) {
		this.inRadius = innerRadius;
		this.outRadius = outerRadius;
	}
	
	@Override
	public void draw() {
		if (provider instanceof Overlay2DProvider)
			((Overlay2DProvider)provider).drawRing(primID, rx, ry, inRadius, outRadius);
	}	
	
}
