package org.dawnsci.common.widgets.gda.function.jexl;

import org.dawnsci.common.widgets.gda.function.descriptors.FunctionInstantiationFailedException;
import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

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

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IContentProposalProvider.class) {
			if (jexl.getEngine() != null) {
				return new ExpressionFunctionProposalProvider(jexl.getEngine().getFunctions());
			}
		}
		return super.getAdapter(adapter);
	}

	public Class<? extends IFunction> getIFunctionClass() {
		return JexlExpressionFunction.class;
	}
}
