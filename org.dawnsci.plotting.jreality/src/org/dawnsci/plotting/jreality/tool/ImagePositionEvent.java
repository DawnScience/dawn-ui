/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.tool;

import java.util.List;

import org.eclipse.dawnsci.plotting.api.jreality.tool.DataPositionEvent;
import org.eclipse.dawnsci.plotting.api.jreality.tool.IImagePositionEvent;


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
