package org.dawnsci.plotting.histogram.functions.classes;

import org.dawnsci.plotting.api.histogram.ITransferFunction;

public abstract class AbstractTransferFunction implements ITransferFunction {

	/**
	 * Method takes a point between 0 and 1 and returns a new mapping value between 0 and 1
	 * @param value the value to map
	 * @return the mapped value
	 */
	public abstract double getPoint(double value);
	
	/**
	 * @param input original value to map
	 * @return byte (C-style usage) 0..255 but due to stupid Java signed bytes will be 
	 *              mapped to -128..127 in Java we have to use short
	 */
	final public short mapToByte(double input) {
		return (short)(255*getPoint(input));
	}

	
	@Override
	public int[] getArray() {
		int[] result = new int[256];
		for (int i = 0; i < result.length; i++) {
			result[i] = (int) (getPoint((double)i/256)*255);
		}
		return result;
	}

}
