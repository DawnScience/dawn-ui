/*-
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.datavis.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.january.dataset.IDataset;

/**
 * Applies an expression given to the variables added to it.
 * 
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 */
public class ExpressionApplier {
	
	IExpressionEngine engine;
	
	/**
	 * Creates the object with an initial context of available Datasets.
	 * @param service
	 * 				The service that will parse and evaluate the expression.
	 * @param variableMap
	 * 					Initial name to dataset map. May be null or empty.
	 */
	public ExpressionApplier(IExpressionService service, Map<String, IDataset> variableMap) {
		if (service == null)
			throw new IllegalArgumentException("Invalid ExpressionService passed to constructor.");
		engine = service.getExpressionEngine();
		
		if (variableMap != null && variableMap.size() > 0)
			addVariables(variableMap);
	}
	
	/**
	 * Creates the object without any initial context
	 * @param service
	 * 				The service that will parse and evaluate the expression.
	 */
	public ExpressionApplier(IExpressionService service) {
		this(service, null);
	}

	/**
	 * Adds a variable to the expression engine.
	 * @param name
	 * 			Name of the variable.
	 * @param value
	 * 			Dataset corresponding to the name.
	 */
	public void addVariable(String name, IDataset value) {
		engine.addLoadedVariable(name, value);
	}
	
	/**
	 * Adds several variables to the expression engine.
	 * @param variableMap
	 * 					Map from variable name to Dataset.
	 */
	public void addVariables(Map<String, IDataset> variableMap) {
		for (Map.Entry<String, IDataset> entry : variableMap.entrySet()) {
			engine.addLoadedVariable(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Calculates the result of the provided expression. Returns null when there is no valid result.
	 * @param expression
	 * 					The {@link String} describing the expression.
	 * @return The Dataset containing the results of the data.
	 */
	public IDataset evaluateData(String expression) {
		if (expression == null || expression.isEmpty())
			return null;
		try {
			engine.createExpression(expression);
		} catch (Exception e) {
			return null;
		}
		
		try {
			Object obj = engine.evaluate();
			if (obj instanceof IDataset)
				return (IDataset) obj;
			else
				return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Lists the variables referenced in the given expression. 
	 * @param expression
	 * 					The expression to be parsed.
	 * @return
	 * 		The names of the referenced variables.
	 */
	public Collection<String> parseForVariables(String expression) {
		if (expression == null || expression.isEmpty())
			return Collections.emptyList();
		try {
			engine.createExpression(expression);
			return engine.getVariableNamesFromExpression();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
	
}
