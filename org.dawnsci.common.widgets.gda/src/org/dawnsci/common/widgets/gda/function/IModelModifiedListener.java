package org.dawnsci.common.widgets.gda.function;

public interface IModelModifiedListener {

	void functionModified(IFunctionModifiedEvent event);
	void parameterModified(IParameterModifiedEvent event);
	void fittedFunctionInvalidated(IFittedFunctionInvalidatedEvent event);

}
