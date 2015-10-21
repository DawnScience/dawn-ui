package $packageName$;

import org.dawnsci.plotting.histogram.functions.classes.AbstractTransferFunction;

public class $className$ extends AbstractTransferFunction {

	@Override
	public int[] getArray() {
		int[] result = new int[256];
		for (int i = 0; i < result.length; i++) {
			result[i] = i;
		}
		return result;
	}

	@Override
	public double getPoint(double value) {
		return value;
	}

}