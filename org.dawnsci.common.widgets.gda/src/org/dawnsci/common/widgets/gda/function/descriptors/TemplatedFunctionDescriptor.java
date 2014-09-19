/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.descriptors;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;

public class TemplatedFunctionDescriptor extends FunctionDescriptor {

	public TemplatedFunctionDescriptor(IFunction describingFunction) {
		super(describingFunction);
	}

	@Override
	public IFunction getFunction() throws FunctionInstantiationFailedException {
		try {
			return getDescribingFunction().copy();
		} catch (Exception e) {
			throw new FunctionInstantiationFailedException("Copy of existing template function failed", e);
		}
	}

}
