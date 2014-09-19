/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.jexl;

import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawnsci.common.widgets.gda.function.descriptors.FunctionInstantiationFailedException;
import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;

public class JexlExpressionFunctionDescriptor extends PlatformObject implements IFunctionDescriptor {
	private JexlExpressionFunction jexl;

	public JexlExpressionFunctionDescriptor() {
		jexl = new JexlExpressionFunction();
	}

	@Override
	public IFunction getFunction() throws FunctionInstantiationFailedException {
		throw new FunctionInstantiationFailedException("Jexl Expressions need a provider to handle the creation, "
				+ "see DefaultFunctionDescriptorProvider.getFunction()");
	}

	@Override
	public String getName() {
		return "Jexl Expression";
	}

	@Override
	public String getDescription() {
		return "Jexl Expression";
	}

	@Override
	public String getLongDescription() {
		return getDescription();
	}

	@Override
	public boolean isOperator() {
		return false;
	}

	public Class<? extends IFunction> getIFunctionClass() {
		return JexlExpressionFunction.class;
	}

	public IExpressionEngine getEngine() {
		return jexl.getEngine();
	}

}
