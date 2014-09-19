/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.algorithm.ui.workflow;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.roi.IROI;

/**
 * Interface used to update programmatically a moml file
 * @author wqk87977
 *
 */
public interface IWorkflowUpdater {

	/**
	 * Method that updates a region actor
	 * @param actorName the unique actor's name
	 * @param roi the region of interest
	 */
	public void updateRegionActor(String actorName, IROI roi);

	/**
	 * Method that updates a scalar actor
	 * @param actorName the unique actor's name
	 * @param scalar value parameter
	 */
	public void updateScalarActor(String actorName, double scalarValue);

	/**
	 * Method that updates a function actor
	 * @param actorName the unique actor's name
	 * @param function parameter
	 */
	public void updateFunctionActor(String actorName, IFunction function);

	/**
	 * Method that reads a scalar actor's parameter
	 * @param actorName the unique actor's name
	 * @param scalar name parameter
	 * @return the parameter value
	 */
	public String getActorParam(String actorName, String paramName);

	/**
	 * Method that reads a function actor
	 * @param actorName the unique actor's name
	 * @return function
	 */
	public IFunction getFunctionFromActor(String actorName);

	/**
	 * Method that updates an actor's parameter with a given value
	 * @param actorName the unique actor's name
	 * @param paramName the parameter name
	 * @param paramValue the parameter value
	 */
	public void updateActorParam(String actorName, String paramName, String paramValue);

	/**
	 * Method that updates an input actor
	 * @param actorName 
	 *              the unique actor's name
	 * @param attributeName
	 *              the unique name of the attribute
	 * @param attributeValues
	 *              key value pair of attribute value name and its corresponding value.
	 *              Must have at least one pair. An attribute can be a Field parameter and
	 *              in that case a Map of key-value pairs is necessary.
	 */
	public void updateInputActor(String actorName, String attributeName, Map<String, String> attributeValues);
}
