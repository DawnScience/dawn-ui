package org.dawnsci.common.widgets.gda.function.internal.model;

import org.dawnsci.common.widgets.gda.function.IFunctionModifiedEvent;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IOperator;

public class FunctionModifiedEvent implements IFunctionModifiedEvent {

	private IFunction beforeFunction;
	private IFunction afterFunction;
	private IOperator parentOperator;
	private int indexInParentOperator;

	public FunctionModifiedEvent(IFunction beforeFunction,
			IFunction afterFunction, IOperator parentOperator,
			int indexInParentOperator) {
		this.beforeFunction = beforeFunction;
		this.afterFunction = afterFunction;
		this.parentOperator = parentOperator;
		this.indexInParentOperator = indexInParentOperator;
	}

	@Override
	public IFunction getBeforeFunction() {
		return beforeFunction;
	}

	@Override
	public IFunction getAfterFunction() {
		return afterFunction;
	}

	@Override
	public IOperator getParentOperator() {
		return parentOperator;
	}

	@Override
	public int getIndexInParentOperator() {
		return indexInParentOperator;
	}
}