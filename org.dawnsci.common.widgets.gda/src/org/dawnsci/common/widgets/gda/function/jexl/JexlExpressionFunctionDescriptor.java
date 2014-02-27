package org.dawnsci.common.widgets.gda.function.jexl;

import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawnsci.common.widgets.gda.function.descriptors.FunctionInstantiationFailedException;
import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptor;
import org.eclipse.core.runtime.PlatformObject;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

public class JexlExpressionFunctionDescriptor extends PlatformObject implements IFunctionDescriptor {
	private JexlExpressionFunction jexl;

	public JexlExpressionFunctionDescriptor() {
		jexl = new JexlExpressionFunction();
	}

	@Override
	public IFunction getFunction() throws FunctionInstantiationFailedException {
		throw new FunctionInstantiationFailedException("Jexl Expressions need a provider to handle the creation, "
				+ "see DefaultFunctionDescriptorProvider.getFunction()");
	}

	@Override
	public String getName() {
		return "Jexl Expression";
	}

	public Class<? extends IFunction> getIFunctionClass() {
		return JexlExpressionFunction.class;
	}

	public IExpressionEngine getEngine() {
		return jexl.getEngine();
	}

}
