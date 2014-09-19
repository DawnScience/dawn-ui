/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.descriptors;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;

public abstract class FunctionDescriptor extends PlatformObject implements
		IFunctionDescriptor {

	/**
	 * Some of the meta data about functions are hidden within instances of the
	 * functions, so we have to make a "dummy" instance of the function to get
	 * things like its name, etc.
	 */
	private IFunction describingFunction;

	public FunctionDescriptor(IFunction describingFunction) {
		this.describingFunction = describingFunction;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getName() {
		return describingFunction.getName();
	}

	@Override
	public String getDescription() {
		return describingFunction.getDescription();
	}

	@Override
	public boolean isOperator() {
		return describingFunction instanceof IOperator;
	}

	@Override
	public String getLongDescription() {
		StringBuilder desc = new StringBuilder(
				describingFunction.getDescription() + System.lineSeparator());

		IParameter[] parameters = describingFunction.getParameters();
		if (parameters != null && parameters.length != 0) {
			desc.append(System.lineSeparator() + "Parameters:"
					+ System.lineSeparator());
			for (IParameter param : describingFunction.getParameters()) {
				desc.append("  " + param.getName() + System.lineSeparator());
			}
		}
		return desc.toString();

	}

	public Class<? extends IFunction> getIFunctionClass() {
		return describingFunction.getClass();
	}

	protected IFunction getDescribingFunction() {
		return describingFunction;
	}
}
