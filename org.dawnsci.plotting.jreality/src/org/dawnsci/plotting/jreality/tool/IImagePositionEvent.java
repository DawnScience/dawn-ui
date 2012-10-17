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

package org.dawnsci.plotting.jreality.tool;

import java.util.List;

public interface IImagePositionEvent extends IDataPositionEvent{

	/**
	 * Bit mask for left mouse button
	 */
	public static final short LEFTMOUSEBUTTON = 1;

	/**
	 * Bit mask for right mouse button
	 */
	public static final short RIGHTMOUSEBUTTON = 2;

	/**
	 * Bit mask for the control key
	 */
	public static final short CTRLKEY = 4;

	/**
	 * Bit mask for the shift key
	 */
	public static final short SHIFTKEY = 8;

	/**
	 * Get the id of the overlay primitive that the mouse is currently on
	 * @return -1 if there is no overlay primitive otherwise its id
	 */
	public abstract int getPrimitiveID();

	/**
	 * Get the ids of the overlay primitive that the mouse is currently on
	 * @return an array, possibly empty, of the primitives that were hit
	 */
	public abstract List<Integer> getPrimitiveIDs();

	/**
	 * // TODO This really should return a double, ie if you can draw on a double (See OverlayProviders) 
	 * // you can also also click on a double
	 * Get the position in the image in pixel coordinates
	 * @return image position
	 */
	public abstract int[] getImagePosition();

	/**
	 * Get the specific bit flags
	 * @return the bit flags
	 */
	public abstract short getFlags();

}
