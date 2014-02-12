package org.dawnsci.plotting.api.histogram;


/**
 * This interface is required for classes which wish to implement the Transfer Function 
 * Extension point.
 * 
 * @author ssg37927
 *
 */
public interface ITransferFunction {

	/**
	 * This method is called when creating a colourmap and the implementing class is being used to 
	 * generate one of the colour channels. Used for 8-bit images.
	 * 
	 * @return the int array containing all the points for the colourscale, currently this should be
	 * a fixed length array at 256.
	 */
	public int[] getArray();	

	/**
	 * Get a value between 0 and 1.
	 * @param value
	 * @return
	 */
	public double getPoint(double value);

	/**
	 * Maps the value to the palette color
	 * @param input
	 * @return
	 */
	public short mapToByte(double input);
}
