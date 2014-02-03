package org.dawnsci.plotting.histogram.functions;

import org.dawnsci.plotting.api.histogram.functions.AbstractMapFunction;

/**
 * This interface is required for classes which wish to implement the Transfer Function 
 * Extension point.
 * 
 * @author ssg37927
 *
 */
public interface ITransferFunctionArrayProvider {

	/**
	 * This method is called when creating a colourmap and the implementing class is being used to 
	 * generate one of the colour channels. Used for 8-bit images.
	 * 
	 * @return the int array containing all the points for the colourscale, currently this should be
	 * a fixed length array at 256.
	 */
	public int[] getArray();	
	
	/**
	 * This method returns the map function, used for 16-bit images and above.
	 * @return
	 */
	public AbstractMapFunction getMapFunction();

}
