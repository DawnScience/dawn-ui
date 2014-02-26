package org.dawnsci.common.widgets.gda.function;

/**
 * A listener for function model modification events
 */
public interface IModelModifiedListener {

	/**
	 * Notifies clients of function modifications
	 * @param event function modification event
	 */
	void functionModified(IFunctionModifiedEvent event);

	/**
	 * Notifies clients of parameter modifications
	 * @param event parameter modification events
	 */
	void parameterModified(IParameterModifiedEvent event);

	/**
	 * Notifies clients if the fitted function has been invalidated
	 * @param event fitted function invalidated event
	 */
	void fittedFunctionInvalidated(IFittedFunctionInvalidatedEvent event);

}
