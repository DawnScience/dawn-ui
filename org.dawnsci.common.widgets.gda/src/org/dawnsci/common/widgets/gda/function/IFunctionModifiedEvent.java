/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;

public interface IFunctionModifiedEvent {

	/**
	 * Return the function prior to the modification. If the modification was
	 * made in place then {@link #getBeforeFunction()} ==
	 * {@link #getAfterFunction()}.
	 * <p>
	 * On a new function being created, returns <code>null</code>
	 *
	 * @return
	 */
	public IFunction getBeforeFunction();

	/**
	 * Return the function after the modification.
	 * <p>
	 * On a new function being deleted, returns <code>null</code>
	 *
	 * @return
	 */
	public IFunction getAfterFunction();

	/**
	 * Return the index of the modified function in the parent operator, if
	 * known. Otherwise return null.
	 *
	 * @return
	 */
	public IOperator getParentOperator();

	/**
	 * Return the index of the modified function in the parent operator. Only
	 * valid if {@link #getParentOperator()} is not null.
	 *
	 * @return
	 */
	public int getIndexInParentOperator();

}
