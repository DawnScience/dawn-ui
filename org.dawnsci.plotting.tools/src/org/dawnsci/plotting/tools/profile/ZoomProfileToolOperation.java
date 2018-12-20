/*
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

public class ZoomProfileToolOperation extends AbstractOperation<ZoomProfileToolModel, OperationData> {

	@Override
	public String getId() {
		return "org.dawnsci.plotting.tools.profile.ZoomProfileToolOperation";
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO;
	}

	@Override
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		RectangularROI bounds = model.getRegion();
		
		final int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;
		final int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;
		
		final Dataset slice = DatasetUtils.convertToDataset(input.getSlice(new int[] { (int) bounds.getPoint()[1],   (int) bounds.getPoint()[0]    },
										                       new int[] { (int) bounds.getEndPoint()[1],(int) bounds.getEndPoint()[0] },
										                       new int[] {yInc, xInc}));

		
		return new OperationData(slice);
	}

}
