/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.internal.model;

import org.dawnsci.common.widgets.gda.function.IParameterModifiedEvent;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;

public final class ParameterModifiedEvent implements
		IParameterModifiedEvent {
	private final ParameterModel parameterModel;

	ParameterModifiedEvent(ParameterModel parameterModel) {
		this.parameterModel = parameterModel;
	}

	@Override
	public IParameter getParameter() {
		return this.parameterModel.getParameter();
	}

	@Override
	public int getIndexInFunction() {
		return this.getIndexInFunction();
	}

	@Override
	public IFunction getFunction() {
		return this.parameterModel.getFunction();
	}

	public ParameterModel getParameterModel() {
		return parameterModel;
	}
}