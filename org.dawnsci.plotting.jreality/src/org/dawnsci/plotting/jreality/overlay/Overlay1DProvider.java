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

/**
 * 1D Overlay provider
 */
public interface Overlay1DProvider extends OverlayProvider {

	/**
	 * Draw a line primitive
	 * @param primID id of the primitive
	 * @param sx start x position
	 * @param sy start y position
	 * @param ex end x position
	 * @param ey end y position
	 * @return if the draw was successful (true) otherwise (false)
	 */
	
	public boolean drawLine(int primID, double sx,  double sy, double ex, double ey);
	
	/**
	 * Draw a box primitive
	 * @param primID id of the primitive
	 * @param lux left upper x coordinate
	 * @param luy left upper y coordinate
	 * @param rlx right lower x coordinate
	 * @param rly right lower y coordinate
	 * @return if the draw was successful (true) otherwise (false)
	 */
	public boolean drawBox(int primID, double lux, double luy, double rlx, double rly);
	
	
	/**
	 * Draw a label primitive 
	 * @param primID id of the primitive
	 * @param sx x coordinate of the label
	 * @param sy y coordinate of the label
	 * @return if the translation was successful (true) otherwise (false)
	 */
	
	public boolean drawLabel(int primID, double sx, double sy);
	
	
}
