/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.widgets.gda.function;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;

public class FunctionExtensionFactory {

	private static final String FUNCTIONS_EXTENSIONS = "org.dawnsci.common.functions";

	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$

	private static FunctionExtensionFactory functionExtensionFactory;

	// Map of Function Names to IConfigurationElements
	protected Map<String, IConfigurationElement> functionsMap = new HashMap<>();



	private FunctionExtensionFactory() {
		final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(
				FUNCTIONS_EXTENSIONS);
		for (final IConfigurationElement e : configs) {
			functionsMap.put(e.getAttribute(ATTR_NAME), e);
		}
	}

	public static synchronized FunctionExtensionFactory getFunctionExtensionFactory() {
		if (functionExtensionFactory == null) {
			functionExtensionFactory = new FunctionExtensionFactory();
		}
		return functionExtensionFactory;
	}

	public String[] getFittingFunctionNames() {
		return functionsMap.keySet().toArray(new String[0]);
	}

	public IFunction getFittingFunction(String name) throws CoreException {
		Object object;
		IConfigurationElement element = functionsMap.get(name);
		object = element.createExecutableExtension(ATTR_CLASS);

		if (object instanceof IFunction) {
			return (IFunction) object;
		}
		return null;

	}
}
