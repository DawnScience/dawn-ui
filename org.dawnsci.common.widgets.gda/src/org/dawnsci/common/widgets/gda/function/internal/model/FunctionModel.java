package org.dawnsci.common.widgets.gda.function.internal.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;

/**
 *
 * Function domain model object. Represents a function
 * and fitted function pair.
 *
 */
public class FunctionModel extends FunctionModelElement {

	private OperatorModel parentModel;
	private IOperator parent;
	private IFunction function;
	private IOperator fittedParent;
	private IFunction fittedFunction;
	private int functionIndex;
	private ParameterModel[] children = new ParameterModel[0];

	public FunctionModel(FunctionModelRoot modelRoot,
			OperatorModel parentModel, IOperator parent,
			IFunction function, IOperator fittedParent,
			IFunction fittedFunction, int functionIndex) {
		super(modelRoot);
		this.parentModel = parentModel;
		this.parent = parent;
		this.function = function;
		this.fittedParent = fittedParent;
		this.fittedFunction = fittedFunction;
		this.functionIndex = functionIndex;
	}

	public FunctionModel(FunctionModelRoot modelRoot,
			OperatorModel parentModel, IOperator parent,
			IFunction function, int functionIndex) {
		this(modelRoot, parentModel, parent, function, null, null,
				functionIndex);
	}

	@Override
	public OperatorModel getParentModel() {
		return parentModel;
	}

	/**
	 * @return the fittedFunction
	 */
	public IFunction getFittedFunction() {
		return fittedFunction;
	}

	/**
	 * @return the parent
	 */
	public IOperator getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(IOperator parent) {
		this.parent = parent;
	}

	/**
	 * @return the function
	 */
	@Override
	public IFunction getFunction() {
		return function;
	}

	/**
	 * @param function
	 *            the function to set
	 */
	public void setFunction(IFunction function) {
		this.function = function;
	}

	/**
	 * @return the fittedParent
	 */
	public IOperator getFittedParent() {
		return fittedParent;
	}

	/**
	 * @param fittedParent
	 *            the fittedParent to set
	 */
	public void setFittedParent(IOperator fittedParent) {
		this.fittedParent = fittedParent;
	}

	/**
	 * FunctionModel is not suitable for hashing in the FunctionTreeViewer, the
	 * only client.
	 */
	@Override
	public int hashCode() {
		return 42;
	}

	/**
	 * The fitted function (existence or value) does not affect the equality.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof FunctionModel))
			return false;
		FunctionModel rhs = (FunctionModel) obj;
		return new EqualsBuilder().append(modelRoot, rhs.modelRoot)
				.append(parentModel, rhs.parentModel)
				.append(parent, rhs.parent).append(function, rhs.function)
				.append(functionIndex, rhs.functionIndex).isEquals();
	}

	@Override
	public ParameterModel[] getChildren() {
		ParameterModel[] recalulatedChildren = new ParameterModel[function
				.getNoOfParameters()];
		for (int i = 0; i < recalulatedChildren.length; i++) {
			recalulatedChildren[i] = new ParameterModel(modelRoot, this,
					function, fittedFunction, i);
			if (i < children.length
					&& children[i].getParameter() == recalulatedChildren[i]
							.getParameter()) {
				recalulatedChildren[i] = children[i];
				recalulatedChildren[i].setFittedFunction(fittedFunction);
			}
		}
		children = recalulatedChildren;
		return children;
	}

	@Override
	public int getFunctionIndexInParentOperator() {
		return functionIndex;
	}

	@Override
	public IOperator getParentOperator() {
		return getParent();
	}

	@Override
	public boolean canEdit() {
		if (getFunction() instanceof JexlExpressionFunction) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getEditingValue() {
		if (getFunction() instanceof JexlExpressionFunction) {
			JexlExpressionFunction jexlExpressionFunction = (JexlExpressionFunction) getFunction();
			return jexlExpressionFunction.getExpression();
		} else {
			return getFunction().getName();
		}
	}

	@Override
	public FunctionModifiedEvent setEditingValue(String value) {
		return newFunctionHelper(parent, value, functionIndex);
	}

	@Override
	public String toSimpleString() {
		return function.getName();
	}

	@Override
	public String toString() {
		String ret = function.toString();
		if (fittedFunction != null) {
			ret += "Fitted " + fittedFunction.toString();
		}
		return ret;
	}

	public void setFittedFunction(IOperator fittedParent,
			IFunction fittedFunction) {
		this.fittedParent = fittedParent;
		this.fittedFunction = fittedFunction;
	}
}
