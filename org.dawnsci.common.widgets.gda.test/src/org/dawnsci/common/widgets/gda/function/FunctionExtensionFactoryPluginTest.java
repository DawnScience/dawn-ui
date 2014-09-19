/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.widgets.gda.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.junit.Test;

public class FunctionExtensionFactoryPluginTest {

	@Test
	public void testGetFittingFunctionNames() {
		String[] fittingFunctionNames = FunctionExtensionFactory.getFunctionExtensionFactory().getFittingFunctionNames();
		List<String> list = Arrays.asList(fittingFunctionNames);
		// test that we see normal/always available function
		assertTrue(list.contains("Gaussian"));
		// test that we see third-party defined function
		assertTrue(list.contains("Kichwa Function"));
	}

	@Test
	public void testGetFittingFunction() throws CoreException {
		IFunction function = FunctionExtensionFactory.getFunctionExtensionFactory().getFittingFunction("Kichwa Function");
		assertEquals("Kichwa Test Function", function.getName());
	}

}
