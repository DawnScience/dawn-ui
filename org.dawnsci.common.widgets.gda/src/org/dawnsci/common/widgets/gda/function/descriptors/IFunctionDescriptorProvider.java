package org.dawnsci.common.widgets.gda.function.descriptors;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

public interface IFunctionDescriptorProvider {
	IFunctionDescriptor getDescriptor(String functionName);
	IFunctionDescriptor getDescriptor(IFunction functionInstance);
	IFunctionDescriptor[] getFunctionDescriptors();
	IFunction getFunction(String functionName) throws FunctionInstantiationFailedException;
}
