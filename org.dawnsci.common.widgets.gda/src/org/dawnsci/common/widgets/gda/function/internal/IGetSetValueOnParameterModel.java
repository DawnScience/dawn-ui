package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;

public interface IGetSetValueOnParameterModel {
	/**
	 * Get the current value from the IParameter.
	 */
	public double getValue(ParameterModel param);

	/**
	 * Return the string the user typed, which is null if the value was not an
	 * error.
	 */
	public String getErrorValue(ParameterModel param);

	public void setValue(ParameterModel param, String value);

}