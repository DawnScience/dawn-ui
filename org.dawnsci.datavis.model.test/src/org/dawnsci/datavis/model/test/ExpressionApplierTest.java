/*-
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.datavis.model.ExpressionApplier;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngineListener;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.Maths;
import org.junit.Test;

public class ExpressionApplierTest {

	IExpressionService service;
	
	@Test
	public void test() {
		int npoints = 100;
		Dataset data = Maths.divide(DatasetFactory.createRange(npoints+1), npoints);
		Dataset target = Maths.sin(data);
		
		// Use the Expression applier
		service = new MockExpressionService();
		ExpressionApplier applier = new ExpressionApplier(service);
		applier.addVariable("data", data);
		Dataset output = (Dataset) applier.evaluateData("sin(data)");
		
		// quantify the RMS difference (it should be zero)
		double rms = output.residual(target);
		
		double targetPrecision = 1e-12;
		
		assertTrue("Evaluated result deviates too far from target", rms < targetPrecision);
		
	}

	
	private class MockExpressionService implements IExpressionService {
		@Override
		public IExpressionEngine getExpressionEngine() {
			return new SineExpressionEngine();
		}
	}

	private class SineExpressionEngine implements IExpressionEngine {

		Map<String, Object> dataMap;
		
		public SineExpressionEngine() {
			dataMap = new HashMap<>();
		}
		
		@Override
		public void createExpression(String expression) throws Exception {
			String targetExpression = "sin(data)";
			if (!expression.equals(targetExpression))
				throw new IllegalArgumentException("Syntax error in expression: try \"" + targetExpression + "\".");
		}

		@Override
		public <T> T evaluate() throws Exception {
			if (dataMap.get("data") instanceof Dataset) {
				return (T) Maths.sin((Dataset) dataMap.get("data"));
			}
			return null;
		}

		@Override
		public void addExpressionEngineListener(IExpressionEngineListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeExpressionEngineListener(IExpressionEngineListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void evaluateWithEvent(IMonitor monitor) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Map<String, Object> getFunctions() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setFunctions(Map<String, Object> functions) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setLoadedVariables(Map<String, Object> variables) {
			dataMap.clear();
			addLoadedVariables(variables);
			
		}

		@Override
		public Object getLoadedVariable(String name) {
			return ("data".equals(name)) ? dataMap.get("data") : null;
		}

		@Override
		public void addLoadedVariables(Map<String, Object> variables) {
			Object value = variables.values().toArray()[0];
			addLoadedVariable("data", value);
		}

		@Override
		public void addLoadedVariable(String name, Object value) {
			// adds some data, that is always called 'data'
			dataMap.put("data", value);
			
		}

		@Override
		public Collection<String> getVariableNamesFromExpression() {
			return Arrays.asList("data");
		}

		@Override
		public Collection<String> getLazyVariableNamesFromExpression() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
