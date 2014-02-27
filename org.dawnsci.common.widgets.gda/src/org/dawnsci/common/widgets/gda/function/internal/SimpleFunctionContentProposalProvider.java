package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.function.descriptors.FunctionDescriptor;
import org.dawnsci.common.widgets.gda.function.descriptors.FunctionInstantiationFailedException;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

/**
 * Content proposal provider for simple functions i.e. not JEXL expression
 * functions
 *
 */
public class SimpleFunctionContentProposalProvider implements IContentProposalProvider {

	private FunctionDescriptor functionDescriptor;

	public SimpleFunctionContentProposalProvider(FunctionDescriptor functionDescriptor) {
		this.functionDescriptor = functionDescriptor;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		IFunction function;
		try {
			function = functionDescriptor.getFunction();
			if (function.getName().length() >= contents.length()
					&& function.getName().substring(0, contents.length()).equalsIgnoreCase(contents)) {
				return new IContentProposal[] { new FunctionContentProposal(function, functionDescriptor) };
			}
		} catch (FunctionInstantiationFailedException e) {
		}
		return new IContentProposal[0];
	}

}
