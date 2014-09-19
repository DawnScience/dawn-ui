/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.selector;

/**
 * Class used to ease the use of ListViewer with respect to 
 * arbitrary beans which may be equal in terms of value.
 * 
 * The bean wrapper has a unique name and avoids these problems.
 */
class BeanWrapper {

	public BeanWrapper(final Object bean) {
		setBean(bean);
	}
	
	// We override hashCode because BeanWrappers are mostly put into hash tables
	// and we have a reasonable hashCode we can calculate.
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bean == null) ? 0 : bean.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		// We very much want all BeanWrappers to compare differently, regardless of their
		// containing bean. Best way is to fall back on Object.equals.
		return super.equals(obj);
	}
	
	private Object bean;
	private String name;
	/**
	 * @return the bean
	 */
	public Object getBean() {
		return bean;
	}
	/**
	 * @param bean the bean to set
	 */
	public void setBean(Object bean) {
		this.bean = bean;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public boolean isValidName() {
		if (name==null)             return false;
		if ("".equals(name.trim())) return false;
		return true;
	}
}

	