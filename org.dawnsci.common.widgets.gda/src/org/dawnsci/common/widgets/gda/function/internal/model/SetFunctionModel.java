package org.dawnsci.common.widgets.gda.function.internal.model;

import org.apache.commons.lang.builder.EqualsBuilder;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IOperator;

/**
 * Function domain model object that represents a placeholder
 * for a new function or operator at a specific index. e.g.
 * as a child of a subtract operation
 *
 */
public class SetFunctionModel extends FunctionModelElement {

	private OperatorModel parentModel;
	private IOperator operator;
	private int functionIndex;

	public SetFunctionModel(FunctionModelRoot modelRoot,
			OperatorModel parentModel, IOperator operator, int functionIndex) {
		super(modelRoot);
		this.parentModel = parentModel;
		this.operator = operator;
		this.functionIndex = functionIndex;
	}

	@Override
	public OperatorModel getParentModel() {
		return parentModel;
	}

	/**
	 * Return the operator that this add new function model is going to add to
	 * @return
	 */
	public IOperator getParent() {
		return operator;
	}

	/**
	 * Get the index of this function in the parent operator
	 *
	 * @return
	 */
	public int getFunctionIndex() {
		return functionIndex;
	}

	/**
	 * SetFunctionModel is not suitable for hashing in the FunctionTreeViewer,
	 * the only client.
	 */
	@Override
	public int hashCode() {
		return 42;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof SetFunctionModel))
			return false;
		SetFunctionModel rhs = (SetFunctionModel) obj;
		return new EqualsBuilder().append(modelRoot, rhs.modelRoot)
				.append(parentModel, rhs.parentModel)
				.append(operator, rhs.operator)
				.append(functionIndex, rhs.functionIndex).isEquals();
	}

	@Override
	public IFunction getFunction() {
		return getParent();
	}

	@Override
	public int getFunctionIndexInParentOperator() {
		return getParentModel().getFunctionIndexInParentOperator();
	}

	@Override
	public IOperator getParentOperator() {
		return getParentModel().getParentOperator();
	}

	@Override
	public boolean canEdit() {
		return true;
	}

	@Override
	public String getEditingValue() {
		return "";
	}

	@Override
	public FunctionModifiedEvent setEditingValue(String value) {
		return newFunctionHelper(operator, value, functionIndex);
	}

	@Override
	public String toString() {
		return "Set function";
	}

	public void run(IFunction function) {
		operator.setFunction(functionIndex, function);

		FunctionModifiedEvent event = new FunctionModifiedEvent(
				null, function, operator, functionIndex);
		modelRoot.fireFunctionModified(event);
	}
}
