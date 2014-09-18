package org.dawnsci.common.widgets.gda.function.internal.model;

import org.dawnsci.common.widgets.gda.function.descriptors.FunctionInstantiationFailedException;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction.JexlExpressionFunctionException;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;

/**
 * Common base element for all objects in the function model
 */
public abstract class FunctionModelElement {
	protected FunctionModelRoot modelRoot;

	public FunctionModelElement(FunctionModelRoot modelRoot) {
		this.modelRoot = modelRoot;
	}

	public abstract FunctionModelElement getParentModel();

	/**
	 * Return the children of this element. Must not return null.
	 *
	 * @return
	 */
	public FunctionModelElement[] getChildren() {
		return new FunctionModelElement[0];
	}

	/**
	 * Get the function that this element represents. If the model is for a
	 * function already returns itself, otherwise returns its container.
	 *
	 * @return
	 */
	public abstract IFunction getFunction();

	/**
	 * Get the index of the function in the parents's array (i.e. the index of
	 * {@link #getFunction()} in {@link #getParentOperator()}
	 *
	 * @return
	 */
	public abstract int getFunctionIndexInParentOperator();

	/**
	 * Get the IOperator of the parent of the value returned by
	 * {@link #getFunction()}
	 *
	 * @return
	 */
	public abstract IOperator getParentOperator();

	public abstract boolean canEdit();

	public abstract String getEditingValue();

	/**
	 * Set the value based on the given string and return the IFunction that was
	 * created or edited.
	 *
	 * @param value
	 *            new value user typed
	 */
	public abstract FunctionModifiedEvent setEditingValue(String value);

	/**
	 * Create a new function and add it to the tree
	 *
	 * @param parent
	 *            parent of the new function
	 * @param functionName
	 *            name to use with the provider to get the new function
	 * @param index
	 *            index in parent of the new function, or < 0 for add a new
	 *            function
	 * @return the newly created function, or the edited function, or null if no
	 *         changes were made
	 */
	protected FunctionModifiedEvent newFunctionHelper(IOperator parent,
			String functionName, int index) {
		if ("".equals(functionName)) {
			// don't make any changes
			return null;
		}

		IFunction function = null;
		IFunction originalFunction = null;
		IFunction[] parentFunctions = parent.getFunctions();
		if (index >= 0 && index < parentFunctions.length) {
			originalFunction = parentFunctions[index];
		}

		try {
			function = modelRoot.getFunctionDescriptorProvider().getFunction(
					functionName);
		} catch (FunctionInstantiationFailedException e) {
			// TODO if the user typed something that can't be recognised
			// at all it means that JexlExpressionFunction is not available
			// and that the string is invalid. We may want to better
			// cope with this error, for now the edit is somewhat silently
			// discarded
			// logger.warn("User supplied function name '" + functionName
			// + "' failed to instantiate any IFunctions", e);
			return null;
		}

		// TODO handle this with a getAdapter and a new interface?
		if (function instanceof JexlExpressionFunction
				&& originalFunction instanceof JexlExpressionFunction) {
			// preserve the function to preserve parameters
			function = originalFunction;
			JexlExpressionFunction jexlFunction = (JexlExpressionFunction) function;
			try {
				jexlFunction.setExpression(functionName);
			} catch (JexlExpressionFunctionException e) {
				// we don't display error here, we handle it in the
				// label provider and via the function's isValid()
			}
		} else {
			if (index < 0) {
				parent.addFunction(function);
				// update index to where it was inserted
				index = parent.getFunctions().length - 1;
			} else {
				parent.setFunction(index, function);

				if (function instanceof IOperator
						&& originalFunction instanceof IOperator) {
					copyChildFunctions((IOperator) originalFunction,
							(IOperator) originalFunction);
				}
			}
		}

		FunctionModifiedEvent event = new FunctionModifiedEvent(
				originalFunction, function, parent, index);
		modelRoot.fireFunctionModified(event);

		return event;
	}

	private void copyChildFunctions(IOperator toOperator, IOperator fromOperator) {
		IFunction[] childFunctions = fromOperator.getFunctions();
		if (toOperator.isExtendible()) {
			for (IFunction childFunction : childFunctions) {
				if (childFunction != null) {
					toOperator.addFunction(childFunction);
				}
			}
		} else {
			int limit = toOperator.getFunctions().length;
			if (limit > childFunctions.length)
				limit = childFunctions.length;
			for (int i = 0; i < limit; i++) {
				toOperator.setFunction(i, childFunctions[i]);
			}
		}
	}

	/**
	 * Return whether the current model element is valid.
	 */
	public boolean isValid() {
		if (!getFunction().isValid()) {
			return false;
		}

		FunctionModelElement[] children = getChildren();
		for (FunctionModelElement child : children) {
			if (!child.isValid()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Delete the selected function
	 */
	public void deleteFunction() {
		IFunction function = getFunction();
		IOperator parentOperator = getParentOperator();
		int indexInParentOperator = getFunctionIndexInParentOperator();
		if (function != null && parentOperator != null
				&& parentOperator.getFunctions().length > indexInParentOperator
				&& indexInParentOperator >= 0) {
			parentOperator.removeFunction(indexInParentOperator);

			modelRoot.fireFunctionModified(new FunctionModifiedEvent(function,
					null, parentOperator, indexInParentOperator));
		}
	}

	public String toSimpleString() {
		return toString();
	}
}
