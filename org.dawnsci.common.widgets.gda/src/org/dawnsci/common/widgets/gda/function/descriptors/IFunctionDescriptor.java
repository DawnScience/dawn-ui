package org.dawnsci.common.widgets.gda.function.descriptors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

/**
 * An interface for providing fitting functions by providing the name and
 * the function itself when required
 *
 */
public interface IFunctionDescriptor extends IAdaptable {

	/**
	 * Instantiate the function and return it
	 * @return the function
	 * @throws FunctionInstantiationFailedException
	 */
	IFunction getFunction() throws FunctionInstantiationFailedException;

	/**
	 * Provides the function name
	 * @return String name
	 */
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
