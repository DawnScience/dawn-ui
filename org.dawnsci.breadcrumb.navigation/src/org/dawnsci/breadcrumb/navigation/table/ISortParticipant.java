/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.table;


public interface ISortParticipant {

	/**
	 * Enable/disable UI during sort
	 * @param enabled
	 */
	void setEnabled(boolean enabled);
	
	/**
	 * Save a search query
	 * @param searchString
	 */
	void saveSearch(String searchString);
	
	/**
	 * The label provider used to get the strings used for the search.
	 * @return
	 */
	AbstractLazyLabelProvider getLabelProvider();
	
}
