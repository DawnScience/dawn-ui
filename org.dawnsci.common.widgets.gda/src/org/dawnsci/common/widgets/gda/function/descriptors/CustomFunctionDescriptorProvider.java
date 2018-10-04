/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.descriptors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.common.widgets.gda.Activator;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunctionDescriptor;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;

import uk.ac.diamond.scisoft.analysis.fitting.functions.JexlExpressionFunction;

public class CustomFunctionDescriptorProvider implements
		IFunctionDescriptorProvider {

	private Map<String, IFunctionDescriptor> nameToDescriptor;
	private HashMap<Class<? extends IFunction>, IFunctionDescriptor> clazzToDescriptor;
	private JexlExpressionFunctionDescriptor jexlDescriptor = null;

	public CustomFunctionDescriptorProvider(IFunction[] funcs, boolean addJexl) {
		FunctionDescriptor[] descriptors = new FunctionDescriptor[funcs.length];
		for (int i = 0; i < descriptors.length; i++) {
			descriptors[i] = new TemplatedFunctionDescriptor(funcs[i]);
		}
		nameToDescriptor = new HashMap<>();
		clazzToDescriptor = new HashMap<>();
		fill(descriptors);
		if (addJexl) {
			jexlDescriptor = new JexlExpressionFunctionDescriptor();
			clazzToDescriptor.put(jexlDescriptor.getIFunctionClass(),
					jexlDescriptor);
		}
	}

	protected void fill(FunctionDescriptor[] descriptors) {
		for (FunctionDescriptor descriptor : descriptors) {
			nameToDescriptor.put(descriptor.getName(), descriptor);
			clazzToDescriptor.put(descriptor.getIFunctionClass(), descriptor);
		}
	}

	@Override
	public IFunction getFunction(String functionName) throws FunctionInstantiationFailedException {
		IFunctionDescriptor descriptor = getDescriptor(functionName);
		if (descriptor != null && descriptor == jexlDescriptor) {
			IExpressionService service;
			try {
				service = Activator.getContext().getService(
						Activator.getContext().getServiceReference(IExpressionService.class));
			} catch (Exception e) {
				throw new FunctionInstantiationFailedException(e);
			}
			return new JexlExpressionFunction(service,functionName);
		} else {
			return descriptor.getFunction();
		}
	}

	@Override
	public IFunctionDescriptor getDescriptor(String functionName) {
		if (nameToDescriptor.containsKey(functionName))
			return nameToDescriptor.get(functionName);
		return jexlDescriptor;
	}

	@Override
	public IFunctionDescriptor getDescriptor(IFunction functionInstance) {
		return clazzToDescriptor.get(functionInstance.getClass());
	}

	@Override
	public IFunctionDescriptor[] getFunctionDescriptors() {
		Collection<IFunctionDescriptor> values = clazzToDescriptor.values();
		return values.toArray(new IFunctionDescriptor[values.size()]);
	}
}
