/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Provides the table control used by the visit navigation to the UI
 * 
 * This is similar to a page view in that that the content of the visit view may
 * be swapped.
 * 
 * @author Matthew Gerring
 *
 */
public interface INavigationDelegate extends IAdaptable {

	/**
	 * Called to create the GUI
	 * @param groupLabel
	 * @param toolTip
	 * @param card
	 */
	public void createContent(String groupLabel, String toolTip, Composite card);
	
	/**
	 * Provides the selection provider!
	 * @return
	 */
	public ISelectionProvider getSelectionProvider();
	
	/**
	 * Set actions required for this UI component inactive or active.
	 * @param isActive
	 */
	public void setActionsActive(boolean isActive, IContributionManager man);

	/**
	 * 
	 * @return the control which holds this table (plus any other controls like those
	 * required for searching the table).
	 */
	public Control getControl();

	/**
	 * Sets the table enabled or disabled
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);

	/**
	 * 
	 * @return true if table is visible
	 */
	public boolean isVisible();

	/**
	 * Refresh the table
	 */
	public void refresh();

	/**
	 * 
	 */
	public void dispose();

	/**
	 * clears the content of the viewer.
	 */
	public void clear();

	/**
	 * 
	 * @param cursorCode
	 */
	public void setCursor(Cursor cursor);
}
