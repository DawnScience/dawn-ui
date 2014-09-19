/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.descriptors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

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
	 * Provides the description of the function
	 *
	 * @return String description of the function
	 */
	String getDescription();

	/**
	 * Provides the long description of the function, for example may include
	 * parameter information
	 *
	 * @return String description of the function
	 */
	String getLongDescription();

	/**
	 * Return true if the described function is an operator. i.e.
	 * {@link #getFunction()} returns something that implements
	 * {@link IOperator}
	 *
	 * @return whether function is an operator
	 */
	boolean isOperator();

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
