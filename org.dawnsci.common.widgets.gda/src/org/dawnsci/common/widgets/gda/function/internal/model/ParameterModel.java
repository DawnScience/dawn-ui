package org.dawnsci.common.widgets.gda.function.internal.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;

/**
 * Represents a parameter in the function model
 *
 */
public class ParameterModel extends FunctionModelElement {
	private FunctionModelElement parentModel;
	private IFunction function;
	private IFunction fittedFunction;
	private int parameterIndex;
	private String errorUpperLimit;
	private String errorLowerLimit;
	private String errorValue;

	public ParameterModel(FunctionModelRoot modelRoot,
			FunctionModelElement parentModel, IFunction function,
			int parameterIndex) {
		this(modelRoot, parentModel, function, null, parameterIndex);
	}

	public ParameterModel(FunctionModelRoot modelRoot,
			FunctionModelElement parentModel, IFunction function,
			IFunction fittedFunction, int parameterIndex) {
		super(modelRoot);
		this.parentModel = parentModel;
		this.function = function;
		this.fittedFunction = fittedFunction;
		this.parameterIndex = parameterIndex;
	}

	@Override
	public FunctionModelElement getParentModel() {
		return parentModel;
	}

	/**
	 * @return the function
	 */
	@Override
	public IFunction getFunction() {
		return function;
	}

	/**
	 * @return the parameter
	 */
	public IParameter getParameter() {
		return function.getParameter(parameterIndex);
	}

	/**
	 * @return the parameter
	 */
	public IParameter getFittedParameter() {
		if (fittedFunction == null)
			return null;
		return fittedFunction.getParameter(parameterIndex);
	}

	/**
	 * @return the parameterIndex
	 */
	public int getParameterIndex() {
		return parameterIndex;
	}

	/**
	 * ParameterModel is not suitable for hashing in the FunctionTreeViewer, the
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
		if (!(obj instanceof ParameterModel))
			return false;
		ParameterModel rhs = (ParameterModel) obj;
		return new EqualsBuilder().append(modelRoot, rhs.modelRoot)
				.append(parentModel, rhs.parentModel)
				.append(function, rhs.function)
				.append(parameterIndex, rhs.parameterIndex).isEquals();
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
		return false;
	}

	@Override
	public String getEditingValue() {
		throw new UnsupportedOperationException(
				"The editing value should not have been fetched because canEdit returned false");
	}

	@Override
	public FunctionModifiedEvent setEditingValue(String value) {
		throw new UnsupportedOperationException(
				"The editing value should not have been set because canEdit returned false");
	}

	@Override
	public String toSimpleString() {
		return getParameter().getName();
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(String.format("%d) %s = %g in range [%g, %g]\n",
				getParameterIndex(), getParameter().getName(), getParameter()
						.getValue(), getParameter().getLowerLimit(),
				getParameter().getUpperLimit()));
		if (fittedFunction != null) {
			out.append("Fitted:\n");
			out.append(String.format("%d) %s = %g in range [%g, %g]\n",
					getParameterIndex(), getFittedParameter().getName(),
					getFittedParameter().getValue(), getFittedParameter()
							.getLowerLimit(), getFittedParameter()
							.getUpperLimit()));
		}
		return out.toString();
	}

	public void setParameterValue(String value) {
		try {
			setParameterValue(parseDouble(value));
		} catch (NumberFormatException e) {
			errorValue = value;
			syncWithOtherParameterModels();
		}
	}

	public void setParameterValue(double value) {
		boolean wasFixed = getParameter().isFixed();
		try {
			getParameter().setFixed(false);
			getParameter().setValue(value);
		} finally {
			getParameter().setFixed(wasFixed);
		}
		function.setDirty(true);
		errorValue = null;
		modelRoot.fireParameterModified(new ParameterModifiedEvent(this));
		syncWithOtherParameterModels();
	}

	public void setParameterUpperLimit(String value) {
		try {
			double parseDouble = parseDouble(value);
			getParameter().setUpperLimit(parseDouble);
			function.setDirty(true);
			errorUpperLimit = null;
			modelRoot.fireParameterModified(new ParameterModifiedEvent(this));
		} catch (NumberFormatException e) {
			errorUpperLimit = value;
		}
		syncWithOtherParameterModels();
	}

	private double parseDouble(String value) {
		if ("Min Double".equalsIgnoreCase(value))
			return -Double.MAX_VALUE;
		else if ("Max Double".equalsIgnoreCase(value))
			return Double.MAX_VALUE;
		return Double.parseDouble((String) value);
	}

	public void setParameterLowerLimit(String value) {
		try {
			double parseDouble = parseDouble(value);
			getParameter().setLowerLimit(parseDouble);
			function.setDirty(true);
			errorLowerLimit = null;
			modelRoot.fireParameterModified(new ParameterModifiedEvent(this));
		} catch (NumberFormatException e) {
			errorLowerLimit = value;
		}
		syncWithOtherParameterModels();
	}

	public void setParameterFixed(boolean fixed) {
		getParameter().setFixed(fixed);
		function.setDirty(true);
		modelRoot.fireParameterModified(new ParameterModifiedEvent(this));
	}

	public boolean isParameterFixed() {
		return getParameter().isFixed();
	}

	public double getParameterValue() {
		return getParameter().getValue();
	}

	public double getParameterLowerLimit() {
		return getParameter().getLowerLimit();
	}

	public double getParameterUpperLimit() {
		return getParameter().getUpperLimit();
	}

	public String getParameterValueError() {
		return errorValue;
	}

	public String getParameterLowerLimitError() {
		if (isParameterFixed())
			return null;
		else
			return errorLowerLimit;
	}

	public String getParameterUpperLimitError() {
		if (isParameterFixed())
			return null;
		else
			return errorUpperLimit;
	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}

		if (isParameterFixed()) {
			return errorValue == null;
		} else {
			return errorValue == null && errorLowerLimit == null
					&& errorUpperLimit == null;
		}
	}

	public void setFittedFunction(IFunction fittedFunction) {
		this.fittedFunction = fittedFunction;
	}

	public void copyFrom(ParameterModel other) {
		IParameter otherParameter = other.getParameter();
		IParameter parameter = getParameter();

		// clear any settings that will disallow/change/warn about changing the
		// settings
		parameter.setFixed(false);
		parameter.setLimits(-Double.MAX_VALUE, Double.MAX_VALUE);

		// apply the settings inside the parameter
		parameter.setValue(otherParameter.getValue());
		parameter.setLimits(otherParameter.getLowerLimit(),
				otherParameter.getUpperLimit());
		parameter.setFixed(otherParameter.isFixed());

		// copy the error settings
		errorValue = other.errorValue;
		errorLowerLimit = other.errorLowerLimit;
		errorUpperLimit = other.errorUpperLimit;

		modelRoot.fireParameterModified(new ParameterModifiedEvent(this));

		syncWithOtherParameterModels();
	}

	/**
	 * Unlike all the other models, there is local state in the ParameterModel.
	 * If the same parameter appears in other places in the tree then we need to
	 * sync them all up.
	 */
	protected void syncWithOtherParameterModels() {
		ParameterModel[] parameterModels = modelRoot.getParameterModel(
				getParameter(), true);
		for (ParameterModel other : parameterModels) {
			if (other == this) {
				// don't update self again
				continue;
			}
			other.errorValue = errorValue;
			other.errorLowerLimit = errorLowerLimit;
			other.errorUpperLimit = errorUpperLimit;

			// TODO should we fire this? the underlying parameter isn't
			// changing again, only the ParameterModel
			modelRoot.fireParameterModified(new ParameterModifiedEvent(
					other));
		}
	}
}
