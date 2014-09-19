/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.views;

import org.eclipse.jface.resource.ImageDescriptor;


/**
 * Normally an enum implements this interface with a finite set of possible table modes.
 * This interface is used at a key for holding delegates, if is not an enum you *must*
 * ensure that hashcode and equals are implemented.
 * 
 * @author fcp94556
 *
 */
public interface INavigationDelegateMode {
	
	/**
	 * Label show to the user for choosing this data view.
	 * @return
	 */
	public String getLabel();

	/**
	 * 
	 * @return boolean to say if should be in toolbar for navigation mode switching,
	 * normally returns true.
	 */
	public boolean isInToolbar();
	
	/**
	 * Tooltip shown over action
	 * @return
	 */
	public String getTooltip();

	/**
	 * 
	 * @return true if icon available for mode.
	 */
	public boolean hasIcon();

	/**
	 * Unique id for mode.
	 * @return
	 */
	public String getId();
	
	/**
	 * Icon or null if not icon.
	 * @return
	 */
	public ImageDescriptor getIcon();
	
	/**
	 * 
	 * @return
	 */
	public INavigationDelegateMode[] allValues();
}