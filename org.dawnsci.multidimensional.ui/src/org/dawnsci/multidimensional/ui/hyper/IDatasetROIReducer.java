/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.multidimensional.ui.hyper;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;

/**
 * Interface for creating an object to reduce an ND array for display in the Hyperwindow
 * 
 */
public interface IDatasetROIReducer {

	/**
	 * Whether the reducer produces 1D (a line plot) or 2D (an image) data
	 * 
	 * @return is1D
	 */
	boolean isOutput1D();
	
	/**
	 * This is the bit that does the juice. Pass in a non-null IMonitor so that
	 * it can be passed down to the slicing. The slicing should be operated from the
	 * UI with a cancelable IMonitor because it can create "infinitely" running jobs
	 * when the ILazyDataset is operating a directory of images.
	 * 
	 * @param data - The multi dimensional dataset
	 * @param axes - a list of the axes, the order in the list corresponds to the order array
	 * @param roi - the ROI to use for slicing
	 * @param slices - how the data is sliced to generate the sub-dataset
	 * @param order - how the dimensions are mapped to the output data
	 * @param monitor
	 * @return
	 * @throws Exception
	 */
	IDataset reduce(ILazyDataset data, List<IDataset> axes, IROI roi, Slice[] slices, int[] order)  throws Exception;
	
	List<RegionType> getSupportedRegionType();
	
	/**
	 * Build the initial ROI for the reducer
	 * 
	 * Order of the axes list here matches the re-arranged order,
	 * not the same as the lazydataset
	 * 
	 * @param axes
	 * @param order
	 * @return
	 */
	IROI getInitialROI(List<IDataset> axes, int[] order);
	
	/**
	 * That the reducer supports multiple regions
	 * 
	 * Usually means that the output is a line, not an image
	 * 
	 * @return
	 */
	boolean supportsMultipleRegions();
	
	
	/**
	 * Get the axes of the reduced data
	 * 
	 * @return
	 */
	List<IDataset> getAxes();
	
	
	/**
	 * Whether the Region/ROI for this reducer needs to be created
	 * or will be created by an external listener
	 * @return
	 */
	default boolean createROI() {
		return true;
	}
	
}
