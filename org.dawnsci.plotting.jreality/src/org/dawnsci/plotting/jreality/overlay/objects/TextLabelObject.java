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
import org.dawnsci.plotting.jreality.overlay.enums.LabelOrientation;

/**
 *
 */
public class TextLabelObject extends OverlayObject {

	private double cx,cy;
	
	public TextLabelObject(int primID, OverlayProvider provider) {
		super(primID, provider);
	}
	
	public void setTextPosition(double cx, double cy) {
		this.cx = cx;
		this.cy = cy;
	}

	public void setTextOrientation(LabelOrientation orient) {
		if (provider != null && primID != -1)
			provider.setLabelOrientation(primID, orient);
	}
	
	public void setText(String text, int alignment) {
		if (provider != null && primID != -1)
			provider.setLabelText(primID, text, alignment);
	}
	
	public void setTextFont(java.awt.Font font) {
		if (provider != null && primID != -1)
			provider.setLabelFont(primID, font);
	}

	@Override
	public void draw() {
		if (provider instanceof Overlay1DProvider)
			((Overlay1DProvider)provider).drawLabel(primID, cx, cy);
		else if (provider instanceof Overlay2DProvider)
			((Overlay2DProvider)provider).drawLabel(primID, cx, cy);
	}		
}

