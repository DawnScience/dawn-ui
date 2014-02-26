package org.dawnsci.common.widgets.gda.function.descriptors;

import org.eclipse.core.runtime.PlatformObject;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

public abstract class FunctionDescriptor extends PlatformObject implements
		IFunctionDescriptor {

	/**
	 * Some of the meta data about functions are hidden within instances of the
	 * functions, so we have to make a "dummy" instance of the function to get
	 * things like its name, etc.
	 */
	private IFunction describingFunction;

	public FunctionDescriptor(IFunction describingFunction) {
		this.describingFunction = describingFunction;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getName() {
		return describingFunction.getName();
	}

	public Class<? extends IFunction> getIFunctionClass() {
		return describingFunction.getClass();
	}

	protected IFunction getDescribingFunction() {
		return describingFunction;
	}
}
