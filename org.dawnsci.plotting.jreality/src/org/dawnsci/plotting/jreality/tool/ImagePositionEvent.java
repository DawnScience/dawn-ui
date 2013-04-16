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


/**
 * An event object that occurs when an Mouse position event is happening
 * inside the Image area
 */
public class ImagePositionEvent extends DataPositionEvent implements IImagePositionEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int imagePosition[];
	private int primitiveID;
	private short flags;
	
	/**
	 * Constructor of an ImagePositionEvent
	 * @param tool tool the event has been constructed from
	 * @param position position in the image as texture coordinates (double)
	 * @param imagePosition position in the image as absolute pixel positions (int)
	 * @param primitiveID if the mouse is currently over a primitive its id
	 * @param flags specific bit flags to encode what device buttons are used
	 * @param mode current mode (start, drag, end)
	 */
	public ImagePositionEvent(ImagePositionTool tool, 
			                  double[] position, 
			                  int[] imagePosition,
			                  int primitiveID,
			                  short flags,
			                  Mode mode)
	{
		super(tool,position,mode);
		this.imagePosition = imagePosition.clone();
		this.primitiveID = primitiveID;
		this.flags = flags;
	}
	
	
	/**
	 * Get the id of the overlay primitive that the mouse is currently add
	 * @return -1 if there is no overlay primitive otherwise its id
	 */
	@Override
	public int getPrimitiveID()
	{
		return primitiveID;
	}
	
	/**
	 * Get the position in the image in pixel coordinates
	 * @return image position
	 */
	@Override
	public int[] getImagePosition()
	{
		return imagePosition;
	}
	
	/**
	 * Get the specific bit flags
	 * @return the bit flags
	 */
	@Override
	public short getFlags() {
		return flags;
	}


	@Override
	public List<Integer> getPrimitiveIDs() {
		throw new UnsupportedOperationException("List of IDs is not implemented. Use getPrimitive");
	}

}
