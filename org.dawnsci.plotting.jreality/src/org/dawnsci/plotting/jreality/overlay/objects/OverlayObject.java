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

import java.awt.Color;

import org.dawnsci.plotting.jreality.overlay.OverlayProvider;
import org.dawnsci.plotting.jreality.overlay.VectorOverlayStyles;

/**
 *
 */
public class OverlayObject {

	protected int primID = -1;
	protected OverlayProvider provider;
	
	public void draw() {
		// Leave this to sub classes to get this right
	}
	
	public OverlayObject(int primID, OverlayProvider provider) 
	{
		this.provider = provider;
		this.primID = primID;
	}
	
	public void setPrimID(int primID) 
	{
		this.primID = primID;
	}
	
	public int getPrimID() 
	{
		return primID;
	}
	
	public void translate(double tx, double ty) 
	{
		if (provider != null)
			provider.translatePrimitive(primID, tx, ty);
	}
	
	public void rotate(double angle, double rcx, double rcy) 
	{
		if (provider != null)
			provider.rotatePrimitive(primID, angle, rcx, rcy);
	}
	
	public void setOutlineColour(Color colour) 
	{
		if (provider != null)
			provider.setOutlineColour(primID, colour);
	}
	
	public void setColour(Color colour) 
	{
		if (provider != null)
			provider.setColour(primID,colour);
	}
	
	public void setTransparency(double transparency) 
	{
		if (provider != null)
			provider.setTransparency(primID, transparency);
	}
	
	public void setStyle(VectorOverlayStyles newStyle) 
	{
		if (provider != null)
			provider.setStyle(primID, newStyle);
	}
	
	public void setAnchorPoints(double x, double y) 
	{
		if (provider != null)
			provider.setAnchorPoints(primID, x, y);
	}
	
	public void setVisible(boolean visible) {
		if (provider != null)
			provider.setPrimitiveVisible(primID, visible);
	}
	
	public void dispose() {
		if (provider != null)
			provider.unregisterPrimitive(primID);
		primID = -1;
	}
	
	@Override
	public void finalize() {
		if (provider != null && primID != -1) {
			provider.unregisterPrimitive(primID);
		}
	}
}
