package org.dawnsci.common.widgets.gda.function.descriptors;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

public class TemplatedFunctionDescriptor extends FunctionDescriptor {

	public TemplatedFunctionDescriptor(IFunction describingFunction) {
		super(describingFunction);
	}

	@Override
	public IFunction getFunction() throws FunctionInstantiationFailedException {
		try {
			return getDescribingFunction().copy();
		} catch (Exception e) {
			throw new FunctionInstantiationFailedException("Copy of existing template function failed", e);
		}
	}

}
