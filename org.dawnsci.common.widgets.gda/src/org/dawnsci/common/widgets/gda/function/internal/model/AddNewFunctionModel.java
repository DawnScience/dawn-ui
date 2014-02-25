package org.dawnsci.common.widgets.gda.function.internal.model;

import org.apache.commons.lang.builder.EqualsBuilder;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IOperator;

public class AddNewFunctionModel extends FunctionModelElement {

	private OperatorModel parentModel;
	private IOperator operator;

	public AddNewFunctionModel(FunctionModelRoot modelRoot,
			OperatorModel parentModel, IOperator parent) {
		super(modelRoot);
		this.parentModel = parentModel;
		this.operator = parent;
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
	 * AddNewFunctionModel is not suitable for hashing in the
	 * FunctionTreeViewer, the only client.
	 */
	@Override
	public int hashCode() {
		return 42;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof AddNewFunctionModel))
			return false;
		AddNewFunctionModel rhs = (AddNewFunctionModel) obj;
		return new EqualsBuilder().append(modelRoot, rhs.modelRoot)
				.append(parentModel, rhs.parentModel)
				.append(operator, rhs.operator).isEquals();
	}

	@Override
	public IFunction getFunction() {
		return getParent();
	}

	@Override
	public int getFunctionIndexInParentOperator() {
		if (getParentModel() == null)
			return 0;
		return getParentModel().getFunctionIndexInParentOperator();
	}

	@Override
	public IOperator getParentOperator() {
		if (getParentModel() == null)
			return null;
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
		return newFunctionHelper(getParent(), value, -1);
	}

	@Override
	public String toString() {
		return "Add new function";
	}

	public void run(IFunction function) {
		operator.addFunction(function);

		FunctionModifiedEvent event = new FunctionModifiedEvent(
				null, function, operator, operator.getNoOfFunctions() - 1);
		modelRoot.fireFunctionModified(event);
	}
}
