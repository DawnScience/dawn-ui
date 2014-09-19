/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.beans;

/**
 * An interface to return the current bean.
 * Can be passed to UI objects which cannot see the actual bean.
 */
public interface BeanProvider {

	/**
	 * Returns the current selected bean or the bean that the 
	 * user is working on.
	 * @return the bean.
	 */
	public Object getBean();
	
	/**
	 * Returns a new bean of the current editing class 
	 * with no values in which is not linked to the system 
	 * and can be used for anything without data being affected.
	 * @return new bean
	 * @throws Exception 
	 */
	public Object getInstance() throws Exception;
}
