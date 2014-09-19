/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.internal.model;

import org.dawnsci.common.widgets.gda.function.IFunctionModifiedEvent;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;

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