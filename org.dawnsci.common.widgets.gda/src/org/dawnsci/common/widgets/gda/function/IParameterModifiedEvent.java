package org.dawnsci.common.widgets.gda.function;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;

public interface IParameterModifiedEvent {

	/**
	 * Return the function the parameter is in, if known. Otherwise return null.
	 *
	 * @return
	 */
	public IFunction getFunction();

	/**
	 * Return the parameter modified.
	 *
	 * @return
	 */
	public IParameter getParameter();

	/**
	 * Return the index of the parameter in the function, valid if
	 * {@link #getFunction()} returns non-null.
	 *
	 * @return
	 */
	public int getIndexInFunction();

}
