/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.file;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

public interface IContain1DData {
	
	public IDataset getxDataset();
	
	public List<IDataset> getyDatasets();
	
	public String getName();
	
	public String getLongName();

}
