/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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