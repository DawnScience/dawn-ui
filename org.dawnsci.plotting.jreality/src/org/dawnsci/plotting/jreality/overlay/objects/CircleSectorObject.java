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
 *
 */
public class CircleSectorObject extends OverlayObject {

	private double cx, cy;
	private double inRadius = 1.0;
	private double outRadius = 2.0;
	private double startAngle = 0;
	private double endAngle = 270.0;
	
	public CircleSectorObject(int primID, OverlayProvider provider) {
		super(primID, provider);
	}
	
	public void setCircleSectorPostion(double cx, double cy) {
		this.cx = cx;
		this.cy = cy;
	}
	
	public void setCircleSectorRadii(double innerRadius, double outerRadius) {
		this.inRadius = innerRadius;
		this.outRadius = outerRadius;
	}
	
	public void setCircleSectorAngles(double startAngle, double endAngle) {
		this.startAngle = startAngle;
		this.endAngle = endAngle;
	}

	@Override
	public void draw() {
		if (provider instanceof Overlay2DProvider)
			((Overlay2DProvider)provider).drawSector(primID, cx, cy, inRadius, outRadius, startAngle, endAngle);
	}	
}
