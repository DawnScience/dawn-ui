/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.beans;

import java.util.Collection;

/**
 * An interface to encapsulate expression support.
 */
public interface IExpressionManager {
	
	/**
	 * 
	 * @return true if expression is valid.
	 */
	public boolean isExpressionValid();
	
	/**
	 * Called to get the value entered by the user as a double. Must call
	 * 
	 * @return the evaluated value or an exception or Double.NaN if the expression is null.
	 */
	public double getExpressionValue();
	
	/**
	 * Sets the expression.
	 * @param expression
	 * 
	 */
    public void setExpression(final String expression);
    
	/**
	 * Returns the current string expression, last called by 
	 * @return expression
	 */
	public String getExpression();
	
	/**
	 * Returns a list of the symbols that can be used in the expression.
	 * This then can be shown in a drop down for the widget for instance.
	 * @return list of symbols.
	 */
	public Collection<String> getAllowedSymbols() throws Exception;
	
	/**
	 * Call to set symbol set which can be used in expressions.
	 * @param symbols
	 * @throws Exception
	 */
	public void setAllowedSymbols(Collection<String> symbols) throws Exception;
}
