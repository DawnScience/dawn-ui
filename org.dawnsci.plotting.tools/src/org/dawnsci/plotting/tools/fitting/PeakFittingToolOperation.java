/*
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.fitting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.metadata.AxesMetadata;

import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;

public class PeakFittingToolOperation extends AbstractOperation<PeakFittingToolModel,OperationData> {

	private List<IdentifiedPeak> identifiedPeaks;
	
	@Override
	public String getId() {
		return "org.dawnsci.plotting.tools.fitting.PeakFittingToolOperation";
	}

	@Override
	public void init() {
		identifiedPeaks = null;
	}
	
	@Override
	public OperationRank getInputRank() {
		return OperationRank.ONE;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.ONE;
	}

	@Override
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {

		final RectangularROI roi = (RectangularROI)model.getRoi();
		if (roi==null) throw new OperationException(this, "Fit region not defined!");

		final double[] p1 = roi.getPointRef();
		final double[] p2 = roi.getEndPoint();

		AxesMetadata md = input.getFirstMetadata(AxesMetadata.class);

		Dataset x;

		if (md == null || md.getAxis(0) == null || md.getAxis(0)[0] == null) {
			x = DatasetFactory.createRange(IntegerDataset.class, input.getSize());
		} else {
			try {
				x = DatasetUtils.convertToDataset(md.getAxis(0)[0].getSlice());
			} catch (DatasetException e) {
				throw new OperationException(this, "Could not read axis!");
			}
		}

		Dataset[] a= Generic1DFitter.selectInRange(x,DatasetUtils.convertToDataset(input),p1[0],p2[0]);
		x = a[0]; Dataset y=a[1];

		// If the IdentifiedPeaks are null, we make them.
		if (identifiedPeaks == null) {
			identifiedPeaks = Generic1DFitter.parseDataDerivative(x, y, FittingUtils.getSmoothing());
		}

		final FittedPeaksInfo info = new FittedPeaksInfo(x, y, monitor);
		info.setIdentifiedPeaks(identifiedPeaks);
		FittedFunctions bean = null;

		try {
			bean = FittingUtils.getFittedPeaks(info);
		} catch (Exception e) {
			//log
			return null;
		}
		
		if (bean == null) {
			return null;
		}

		List<IDataset> output = new ArrayList<>();

		int index = 1;
		for (FittedFunction fp : bean.getFunctionList()) {

			final String peakName = "Peak"+index;
			Dataset sdd = DatasetFactory.createFromObject(fp.getPeakValue(), 1);
			sdd.setName(peakName+"_fit");
			output.add(sdd);

			sdd = DatasetFactory.createFromObject(fp.getPosition(), 1);
			sdd.setName(peakName+"_xposition");
			output.add(sdd);

			sdd = DatasetFactory.createFromObject(fp.getFWHM(), 1);
			sdd.setName(peakName+"_fwhm");
			output.add(sdd);

			sdd = DatasetFactory.createFromObject(fp.getArea(), 1);
			sdd.setName(peakName+"_area");
			output.add(sdd);

			final Dataset[] pair = fp.getPeakFunctions();
			Dataset     function = pair[1];
			Dataset fc = function.clone();
			fc.setName(peakName+"_function");
			output.add(fc);

			++index;
		}

		return new OperationData(input, (Serializable[])output.toArray(new IDataset[output.size()]));
	}

}
