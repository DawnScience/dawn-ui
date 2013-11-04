/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.api.tool;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * Interface used to add extra dataset to a tool
 * @author wqk87977
 *
 */
public interface IAuxiliaryToolDataset {

	/**
	 * Adds dataset to the tool
	 * @param data
	 */
	void addDataset(IDataset data);

	/**
	 * removes dataset
	 * @param data
	 */
	void removeDataset(IDataset data);
}
