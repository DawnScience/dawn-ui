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
