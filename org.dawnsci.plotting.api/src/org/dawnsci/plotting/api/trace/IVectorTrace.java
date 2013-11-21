/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.api.trace;

import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * A vector trace can be added to a 1D or 2D plot to show a vector map over
 * the top. It requires a 3D dataset where the first two coordinates are the 
 * position in x and y and the third coordinate is a size two argument. This
 * represents a magnitude and angle (anti-clockwise from 12 O'Clock) in radians 
 * for the vector arrow.
 * 
 * @author fcp94556
 *
 */
public interface IVectorTrace extends ITrace {
	
	public enum VectorNormalizationType {
		LINEAR, 
		LOGARITHMIC;
	};
	
	/**
	 * Used when the values of the vectors are normalized to the maximum
	 * arrow size.
	 * @param type
	 */
	public void setVectorNormalizationType(VectorNormalizationType type);
	
	/**
	 * Used when the values of the vectors are normalized to the maximum
	 * arrow size.
	 * @return VectorNormalizationType
	 */
	public VectorNormalizationType getVectorNormalizationType();
	
	/**
	 * The maximum size of the arrow in screen pixels. Default is 20.
	 * @param screenPixels
	 */
	public void setMaximumArrowSize(int screenPixels);
	
	/**
	 * The maximum size of the arrow in screen pixels. Default is 20.
	 * @param screenPixels
	 */
	public int getMaximumArrowSize();

	/**
	 * Set the rgb values for the arrow color. By default the arrow is drawn black.
	 * @param rgb
	 */
	public void setArrowColor(int... rgb);
	
	/**
	 * Get the rgb values for the arrow color. By default the arrow is drawn black.
	 * @param rgb
	 */
	public int[] getArrowColor();


	/**
	 * Call to set image data
	 * @param vectors a 3D dataset [x,y,2] where the first two coordinates are the 
     * position in x and y and the third coordinate is a size two argument. This
     * represents a magnitude and angle for the vector arrow.
     * @param axes or null.
	 * @return false if could not set data
	 */
	public boolean setData(final IDataset vectors, final List<IDataset> axes);
	
	/**
	 * 
	 * @return the axes used to look up the position which the vector should be drawn.
	 */
	public List<IDataset> getAxes();
		
	/**
	 * Call to redraw the image, normally the same as repaint on Figure.
	 */
	public void repaint();
	
}
