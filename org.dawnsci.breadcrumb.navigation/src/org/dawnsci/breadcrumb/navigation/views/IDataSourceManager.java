/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.views;

import javax.swing.tree.TreeNode;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.ui.IMemento;


public interface IDataSourceManager {

    /**
     * Create manager from memento.
     * @param memento might be null or have nothing in it
     */
	void init(IMemento memento);

	/**
	 * Called to save any state about the connection.
	 * @param memento
	 */
	void saveState(IMemento memento);

	/**
	 * Call to connect to the data source.
	 */
	void connect();
	
    /**
     * Call to logoff permanently.
     */
	void logoff();


	/**
	 * Call to get the bean that the data source uses.
	 * @return
	 */
	Object getBean();

	/**
	 * 
	 * @return true if the data source is connected
	 */
	boolean isConnected();

	/**
	 * The content for the breadcrumb viewer widget.
	 * @return
	 */
	TreeNode createContent();


	/**
	 * Create actions to logon and logoff.
	 * @param man
	 */
	public void createLogActions(IContributionManager man);

}
