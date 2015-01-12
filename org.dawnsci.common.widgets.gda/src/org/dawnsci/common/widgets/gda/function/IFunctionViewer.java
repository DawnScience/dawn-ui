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
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.jface.viewers.ISelectionProvider;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;

public interface IFunctionViewer extends ISelectionProvider {

	/**
	 * Set the input operator for this viewer
	 *
	 * @param operator
	 *            the operator (an Add - composite function)
	 */
	void setInput(Add operator);

	/**
	 * The the fitted/result function that matches the input.
	 *
	 * @param fittedCompositeFunction
	 *            the operator (an Add)
	 */
	void setFittedInput(Add fittedCompositeFunction);

	/**
	 * Convert the selection that is in the internal model format to the
	 * equivalent IFunction/IParameter. If the selection is a parameter, set
	 * function tree item or add function tree item the containing function is
	 * returned.
	 *
	 * @return selected function or operator
	 */
	IFunction getSelectedFunction();

	/**
	 * Get the parameter that is currently selected, or <code>null</code> if no
	 * parameter is selected. Use getSelectedFunction() to obtain which function
	 * this parameter is part of.
	 *
	 * @return selected parameter
	 */
	IParameter getSelectedParameter();

	/**
	 * Get the index of the parameter within its containing function.
	 *
	 * @return selected index, only valid if {@link #getSelectedParameter()}
	 *         returns non-null
	 */
	int getSelectedParameterIndex();

	/**
	 * Refresh the entire viewer.
	 */
	void refresh();

	/**
	 * Expand all functions in the tree, starting with the root.
	 */
	public void expandAll();

	IOperator getSelectedFunctionParent();

	int getSelectedFunctionParentIndex();

	void addModelModifiedListener(IModelModifiedListener modelModifiedListener);

	void removeModelModifiedListener(
			IModelModifiedListener modelModifiedListener);

	boolean isValid();
	public boolean isFittedValid();
}
