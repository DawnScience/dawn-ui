package org.dawnsci.common.widgets.gda.function.internal.model;

import org.apache.commons.lang.builder.EqualsBuilder;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IOperator;

/**
 * Represents an operator in the function model
 *
 */
public class OperatorModel extends FunctionModelElement {

	private OperatorModel parentModel;
	private IOperator parent;
	private IOperator operator;
	private IOperator fittedParent;
	private IOperator fittedOperator;
	private int operatorIndex;
	private FunctionModelElement[] children = new FunctionModelElement[0];

	public OperatorModel(FunctionModelRoot modelRoot,
			OperatorModel parentModel, IOperator parent, IOperator operator,
			IOperator fittedParent, IOperator fittedOperator, int operatorIndex) {
		super(modelRoot);
		this.parentModel = parentModel;
		this.parent = parent;
		this.operator = operator;
		this.fittedParent = fittedParent;
		this.fittedOperator = fittedOperator;
		this.operatorIndex = operatorIndex;
	}

	public OperatorModel(FunctionModelRoot modelRoot,
			OperatorModel parentModel, IOperator parent, IOperator operator,
			int operatorIndex) {
		this(modelRoot, parentModel, parent, operator, null, null,
				operatorIndex);
	}

	@Override
	public OperatorModel getParentModel() {
		return parentModel;
	}

	/**
	 * @return the fittedFunction
	 */
	public IOperator getFittedOperator() {
		return fittedOperator;
	}

	/**
	 * @return the parent
	 */
	public IOperator getParent() {
		return parent;
	}

	/**
	 * @return the function
	 */
	public IOperator getOperator() {
		return operator;
	}

	@Override
	public IOperator getFunction() {
		return getOperator();
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
	 * OperatorModel is not suitable for hashing in the FunctionTreeViewer, the
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
		if (!(obj instanceof OperatorModel))
			return false;
		OperatorModel rhs = (OperatorModel) obj;
		return new EqualsBuilder().append(modelRoot, rhs.modelRoot)
				.append(parentModel, rhs.parentModel)
				.append(parent, rhs.parent).append(operator, rhs.operator)
				.append(operatorIndex, rhs.operatorIndex).isEquals();
	}

	@Override
	public FunctionModelElement[] getChildren() {
		children = FunctionModelRoot.getChildren(children, this, operator,
				fittedOperator, modelRoot);
		return children;
	}

	/**
	 * Get the index of this function in the parent operator
	 *
	 * @return
	 */
	public int getOperatorIndex() {
		return operatorIndex;
	}

	@Override
	public int getFunctionIndexInParentOperator() {
		return getOperatorIndex();
	}

	@Override
	public IOperator getParentOperator() {
		return getParent();
	}

	@Override
	public boolean canEdit() {
		return false;
	}

	@Override
	public String getEditingValue() {
		return getOperator().getName();
	}

	@Override
	public FunctionModifiedEvent setEditingValue(String value) {
		return newFunctionHelper(parent, value, operatorIndex);
	}

	@Override
	public String toSimpleString() {
		return operator.getName();
	}

	@Override
	public String toString() {
		String ret = operator.toString();
		if (fittedOperator != null) {
			ret += "\nFitted:\n" + fittedOperator.toString();
		}
		return ret;
	}

	public void setFittedOperator(IOperator fittedParent,
			IOperator fittedOperator) {
		this.fittedParent = fittedParent;
		this.fittedOperator = fittedOperator;
	}

	public void addFunction(IFunction newChild) {
		if (operator.isExtendible()
				|| (operator.getNoOfFunctions() < operator.getFunctions().length)) {
			operator.addFunction(newChild);
			modelRoot.fireFunctionModified(new FunctionModifiedEvent(null,
					newChild, operator, operator.getNoOfFunctions() - 1));
		}
	}
}
