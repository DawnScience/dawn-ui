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

import org.dawnsci.plotting.jreality.overlay.enums.LabelOrientation;
import org.dawnsci.plotting.jreality.overlay.primitives.PrimitiveType;
/**
 * A generic overlay provider class
 */
public interface OverlayProvider {

	/**
	 * Begin overlay operation
	 * @param type on what type
	 * @return was a begin successful (true if yes otherwise false)
	 */
	public boolean begin(OverlayType type);
	
	/**
	 * End / flush overlay operation
	 * @param type on what type
	 */
	public void end(OverlayType type);
	
	/**
	 * Register a new primitive to the overlay provider
	 * @param primType type of the primitive
	 * @return the primitive id
	 */
	public int registerPrimitive(PrimitiveType primType);
	
	/**
	 * Register a new primitive to the overlay provider
	 * @param primType type of the primitive
	 * @param fixedSize should it be fixed in size (invariant to scaling)
	 * @return the primitive id
	 */
	public int registerPrimitive(PrimitiveType primType, boolean fixedSize);
	
	/**
	 * Unregister a primitive from the overlay provider
	 * @param primID id of the primitive
	 */
	public void unregisterPrimitive(int primID);
	
	/**
	 * Bulk unregister a list of primitives from the overlay provider
	 * @param ids List of primitive ids stored as integers
	 */
	
	public void unregisterPrimitive(List<Integer> ids);
	
	/**
	 * Set the colour of a primitive
	 * @param primID id of the primitive
	 * @param colour new colour of the primitive
	 */
	public void setColour(int primID, java.awt.Color colour);
	
	/**
	 * Set the outline colour of a primitive
	 * @param primID id of the primitive
	 * @param colour outline colour of the primitive
	 */
	
	public void setOutlineColour(int primID, java.awt.Color colour);
	
	
	/**
	 * Set the line or outline thickness of a primitive
	 * @param primID id of the primitive
	 * @param thickness thickness value 
	 */
	
	public void setLineThickness(int primID, double thickness);
	
	/**
	 * Set the style of a primitive
	 * @param primID id of the primitive
	 * @param newStyle new style of the primitive
	 */
	public void setStyle(int primID, VectorOverlayStyles newStyle);
	

	/**
	 * Set the transparency of the primitive
	 * @param primID id of the primitive
	 * @param transparency value
	 * @return if the transparency was set successfully (true) otherwise (false)
	 */
	public boolean setTransparency(int primID, double transparency);
	
	/**
	 * Set the transparency of the primitive outline if it has outline style
	 * @param primID id of the primitive
	 * @param transparency value
	 * @return if the transparency was set successfully (true) otherwise (false)
	 */
	public boolean setOutlineTransparency(int primID, double transparency);

	/**
	 * Set a primitive to visible or invisible
	 * @param primiD id of the primitive
	 * @param visible should the primitive be visible (true) or invisible (false)
	 * @return if the primitive visibility was changed successfully (true) 
	 *         otherwise (false)
	 */
	public boolean setPrimitiveVisible(int primiD, boolean visible);

	/**
	 * Set a label text
	 * @param primID id of the primitive
	 * @param text String that should be displayed in the label
	 * @param alignment Alignment of the text (see javax.swing.SwingConstants)
	 * @return if the primitive visibility was changed successfully (true) 
	 *         otherwise (false)
	 */	
	public boolean setLabelText(int primID, String text, int alignment);
	
	/**
	 * Set a label Font
	 * @param primID id of the primitive
	 * @param font Font that should be used in the label
	 * @return if the primitive visibility was changed successfully (true) 
	 *         otherwise (false)
	 */	
	
	public boolean setLabelFont(int primID, java.awt.Font font);
	
	/**
	 * Set a label text orientation
	 * @param primID id of the primitive
	 * @param orient Label orientation (Vertical or horizontal)
	 * @return if the primitive visibility was changed successfully (true) 
	 *         otherwise (false)
	 */	
	
	public boolean setLabelOrientation(int primID, LabelOrientation orient);
	
	/**
	 * Rotate a primitive
	 * @param primID id of the primitive
	 * @param angle rotation angle in radians
	 * @param rcx rotation centre x coordinate
	 * @param rcy rotation centre y coordinate
	 */
	public void rotatePrimitive(int primID, double angle, double rcx, double rcy);
	
	/**
	 * Translate a primitive 
	 * @param primID id of the primitive
	 * @param tx translation in x direction
	 * @param ty translation in y direction
	 */
	public void translatePrimitive(int primID, double tx, double ty);

	/**
	 * Set the anchor points for a primitive that is fixed to scaling
	 * @param primID id of the primitive
	 * @param x coordinate of the anchor point
	 * @param y coordinate of the anchor point
	 */
	
	public void setAnchorPoints(int primID, double x, double y);
	
	public void setPlotAreaCursor(int cursor);
	
	public void restoreDefaultPlotAreaCursor();
	
	
}
