package org.dawnsci.rcp.functions.classes;

import org.dawnsci.rcp.functions.ITransferFunctionArrayProvider;

/**
 * Very basic transfer function which provides a simple squared function
 * @author ssg37927
 *
 */
public class SquaredTransferFunction implements ITransferFunctionArrayProvider {

	public SquaredTransferFunction() {
	}

	@Override
	public int[] getArray() {
		int[] result = new int[256];
		for (int i = 0; i < result.length; i++) {
			result[i] = (int) (Math.pow(((double)i/256),2)*256);
		}
		return result;
	}

}
