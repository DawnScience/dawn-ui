package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.function.descriptors.FunctionDescriptor;
import org.dawnsci.common.widgets.gda.function.jexl.ExpressionFunctionProposalProvider;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunctionDescriptor;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

@SuppressWarnings("rawtypes")
public class FunctionContentProposalAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IContentProposalProvider.class)
			if (adaptableObject instanceof JexlExpressionFunctionDescriptor) {
				JexlExpressionFunctionDescriptor jexlDesc = (JexlExpressionFunctionDescriptor) adaptableObject;
				if (jexlDesc.getEngine() != null) {
					return new ExpressionFunctionProposalProvider(jexlDesc.getEngine().getFunctions());
				}
			} else if (adaptableObject instanceof FunctionDescriptor) {
				return new SimpleFunctionContentProposalProvider((FunctionDescriptor)adaptableObject);
			}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] {IContentProposalProvider.class};
	}
}
