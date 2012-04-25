package org.dawnsci.rcp.functions.classes;

import org.dawnsci.rcp.functions.ITransferFunctionArrayProvider;

public abstract class AbstractTransferFunction implements ITransferFunctionArrayProvider {

	/**
	 * Method takes a point between 0 and 1 and returns a new mapping value between 0 and 1
	 * @param value the value to map
	 * @return the mapped value
	 */
	public abstract double getPoint(double value);
	
	@Override
	public int[] getArray() {
		int[] result = new int[256];
		for (int i = 0; i < result.length; i++) {
			result[i] = (int) (getPoint((double)i/256)*255);
		}
		return result;
	}

}
