/*-
 * Copyright 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools.finding;

import java.io.Serializable;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperationBase;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;

public class PeakfindingOperation  extends AbstractOperationBase<PeakfindingModel, OperationData> {

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "org.dawnsci.plotting.tools.finding.PeakFindingOperation";
	}

	@Override
	public OperationRank getInputRank() {
		// TODO Auto-generated method stub
		return OperationRank.SAME;
	}

	@Override
	public OperationRank getOutputRank() {
		// TODO Auto-generated method stub
		return OperationRank.SAME;
	}

	@Override
	public OperationData execute(IDataset slice, IMonitor monitor) throws OperationException {
		//TODO: assumes 1D
		
		try {
			Dataset d = DatasetUtils.convertToDataset(slice);
			if (d.getRank() != 1) {
				d = d.getSliceView().squeeze(true);
			}
//			List<CompositeFunction> fittedPeakList = Generic1DFitter.fitPeakFunctions(DatasetUtils.convertToDataset(model.getxAxis()), 
//					                                                                  d, 
//					                                                                  model.getPeak(), model.createOptimizer(),
//					                                                                  model.getSmoothing(), model.getNumberOfPeaks(),
//					                                                                  model.getThreshold(), 
//					                                                                  model.isAutostopping(), model.isBackgrounddominated(), monitor);
//			
//			
//			//model.peaksId
			
	        // Same original data but with some fitted peaks added to auxillary data.
			return null;
		
		} catch (Exception ne) {
			throw new OperationException(this, ne);
		}
	}

	
	
}
