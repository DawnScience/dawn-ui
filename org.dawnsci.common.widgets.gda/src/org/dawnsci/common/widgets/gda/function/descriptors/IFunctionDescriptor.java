package org.dawnsci.common.widgets.gda.function.descriptors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

public interface IFunctionDescriptor extends IAdaptable {

	IFunction getFunction() throws FunctionInstantiationFailedException;

	String getName();

	/**
	 * Function Descriptors can choose to adapt to:
	 * <ul>
	 * <li> {@link IContentProposalProvider} - if the function descriptor is
	 * going to contribute to auto-completion suggestions.
	 * </ul>
	 *
	 * @param clazz
	 * @return
	 */
	@Override
	Object getAdapter(@SuppressWarnings("rawtypes") Class clazz);
}
