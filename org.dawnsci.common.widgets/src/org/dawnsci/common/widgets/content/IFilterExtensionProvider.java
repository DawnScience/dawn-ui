/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * NOTE There is a copy of this file in a GDA plugin with a GDA license
 * however this code was originally developed outside the GDA and should
 * not have a GDA licence.
 */ 
package org.dawnsci.common.widgets.content;

/**
 *
 */
public interface IFilterExtensionProvider {

	/**
	 * The folder syntax is a regular expression but the
	 * . character is automatically escaped.
	 * @return filter strings e.g. {*.xml, *.txt}
	 */
	public String [] getFilterExtensions();
}
