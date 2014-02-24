package org.dawnsci.common.widgets.gda.function;

public abstract class ModelModifiedAdapter implements IModelModifiedListener {

	protected void modelModified() {
		// do nothing
	}

	@Override
	public void functionModified(IFunctionModifiedEvent event) {
		modelModified();
	}

	@Override
	public void parameterModified(IParameterModifiedEvent event) {
		modelModified();
	}

	@Override
	public void fittedFunctionInvalidated(IFittedFunctionInvalidatedEvent event) {
		modelModified();
	}
}