/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.workbench.ui.data;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;

public interface IPythonFilter {
	/**
	 * 
	 * @param x
	 * @param y
	 * @return new x and y in array
	 * @throws AnalysisRpcException
	 */
	public IDataset[] filter1D(IDataset x,    IDataset       y)      throws AnalysisRpcException;
	
	/**
	 * 
	 * @param image
	 * @param xaxis
	 * @param yaxis
	 * @return new image, xaxis and yaxis in array.
	 * @throws AnalysisRpcException
	 */
	public IDataset[] filter2D(IDataset image, IDataset xaxis, IDataset yaxis) throws AnalysisRpcException;
}
