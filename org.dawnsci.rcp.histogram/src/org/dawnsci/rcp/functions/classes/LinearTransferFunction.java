package org.dawnsci.rcp.functions.classes;

import org.dawnsci.rcp.functions.ITransferFunctionArrayProvider;

/**
 * Very basic transfer function definition which provides a simple linear function
 * @author ssg37927
 *
 */
public class LinearTransferFunction implements ITransferFunctionArrayProvider {

	public LinearTransferFunction() {
	}

	@Override
	public int[] getArray() {
		int[] result = new int[256];
		for (int i = 0; i < result.length; i++) {
			result[i] = i;
		}
		return result;
	}

}
