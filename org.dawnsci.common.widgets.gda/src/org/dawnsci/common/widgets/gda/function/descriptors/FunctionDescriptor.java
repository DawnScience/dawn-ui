package org.dawnsci.common.widgets.gda.function.descriptors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

public abstract class FunctionDescriptor extends PlatformObject implements
		IFunctionDescriptor, IContentProposalProvider {

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
	public IContentProposal[] getProposals(String contents, int position) {
		if (getName().length() >= contents.length()
				&& getName().substring(0, contents.length()).equalsIgnoreCase(
						contents)) {
			return new IContentProposal[] { new FunctionContentProposal() };
		}
		return new IContentProposal[0];
	}

	protected class FunctionContentProposal implements IContentProposal,
			IAdaptable {

		public FunctionContentProposal() {
			super();
		}

		@Override
		public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
			return FunctionDescriptor.this.getAdapter(adapter);
		}

		@Override
		public String getContent() {
			return FunctionDescriptor.this.getName();
		}

		@Override
		public int getCursorPosition() {
			return FunctionDescriptor.this.getName().length();
		}

		@Override
		public String getLabel() {
			return FunctionDescriptor.this.getName();
		}

		@Override
		public String getDescription() {
			return FunctionDescriptor.this.getDescribingFunction()
					.getDescription();
		}

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
