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
public class BoxObject extends OverlayObject {

	private double lux,luy,rlx,rly;
	
	public BoxObject(int primID, OverlayProvider provider) 
	{
		super(primID, provider);
	}

	public void setBoxPoints(double lux, double luy, double rlx, double rly) 
	{
		this.lux = lux;
		this.luy = luy;
		this.rlx = rlx;
		this.rly = rly;
	}
	
	public void setBoxUpperLeftPoint(double lux, double luy)  
	{
		this.lux = lux;
		this.luy = luy;
	}
	
	public void setBoxBottomRightPoint(double rlx, double rly) 
	{
		this.rlx = rlx;
		this.rly = rly;
	}
	
	public void setBoxPosition(double lux, double luy) 
	{
		this.lux = lux;
		this.luy = luy;
	}
	
	public void setBoxWidth(double width) 
	{
		this.rlx = lux + width;
	}
	
	public void setBoxHeight(double height) 
	{
		this.rly = luy + height;
	}
	
	@Override
	public void draw() {
		if (provider instanceof Overlay1DProvider) {
			((Overlay1DProvider)provider).drawBox(primID, lux, luy, rlx, rly);
		} else if (provider instanceof Overlay2DProvider)
			((Overlay2DProvider)provider).drawBox(primID, lux, luy, rlx, rly);
	}	
	
}
