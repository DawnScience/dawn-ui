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

import org.dawb.common.services.ServiceManager;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunctionDescriptor;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;

public class DefaultFunctionDescriptorProvider implements
		IFunctionDescriptorProvider {
	private Map<String, IFunctionDescriptor> nameToDescriptor;
	private HashMap<Class<? extends IFunction>, IFunctionDescriptor> clazzToDescriptor;
	private JexlExpressionFunctionDescriptor jexlDescriptor;

	public DefaultFunctionDescriptorProvider() {
		nameToDescriptor = new HashMap<>();
		clazzToDescriptor = new HashMap<>();
		fill(PluginFunctionDescriptor.getDescriptors());
		jexlDescriptor = new JexlExpressionFunctionDescriptor();
		clazzToDescriptor.put(jexlDescriptor.getIFunctionClass(),
				jexlDescriptor);
	}

	protected void fill(FunctionDescriptor[] descriptors) {
		for (FunctionDescriptor descriptor : descriptors) {
			nameToDescriptor.put(descriptor.getName(), descriptor);
			clazzToDescriptor.put(descriptor.getIFunctionClass(), descriptor);
		}
	}

	@Override
	public IFunctionDescriptor getDescriptor(String functionName) {
		if (nameToDescriptor.containsKey(functionName))
			return nameToDescriptor.get(functionName);
		return jexlDescriptor;
	}

	@Override
	public IFunction getFunction(String functionName) throws FunctionInstantiationFailedException {
		IFunctionDescriptor descriptor = getDescriptor(functionName);
		if (descriptor == jexlDescriptor) {
			IExpressionService service;
			try {
				service = (IExpressionService)ServiceManager.getService(IExpressionService.class);
			} catch (Exception e) {
				throw new FunctionInstantiationFailedException(e);
			}
			return new JexlExpressionFunction(service,functionName);
		} else {
			return descriptor.getFunction();
		}
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
