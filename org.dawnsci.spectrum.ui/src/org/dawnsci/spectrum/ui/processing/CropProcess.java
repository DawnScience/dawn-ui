/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.processing;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;

import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;

public class CropProcess extends AbstractProcess {
	
	double[] range = new double[2];

	public double[] getRange() {
		return range;
	}

	public void setRange(double[] range) {
		this.range = range;
	}

	
	public List<IContain1DData> process(List<IContain1DData> list) {

		List<IContain1DData> output = new ArrayList<IContain1DData>();

		for (IContain1DData data : list) {

			List<IDataset> out = new ArrayList<IDataset>();

			Dataset x = DatasetUtils.convertToDataset(data.getxDataset());

			int[] indic = getCropStartStop(x);
			if (indic[0] == indic[1]) throw new IllegalArgumentException("Invalid crop range");
			int[] start = new int[] {indic[0]};
			int[] stop = new int[] {indic[1]};
			
			//TODO Check start stop not the same
			
			Dataset xc = x.getSlice(start, stop, null);
			
			for (IDataset y : data.getyDatasets()) {

				out.add(y.getSlice(start, stop, null));
			}

			output.add(new Contain1DDataImpl(xc, out, data.getName() + getAppendingName(), data.getLongName() + getAppendingName()));
		}

		return output;
	}
	

	private int[] getCropStartStop(Dataset x) {
		
		double rMin = Math.min(range[0], range[1]);
		double rMax = Math.max(range[0], range[1]);
		
		double min = x.min().doubleValue();
		double max = x.max().doubleValue();
		
		min = rMin < min ? min : rMin;
		max = rMax > max ? max : rMax;
		
		int minPos = ROISliceUtils.findPositionOfClosestValueInAxis(x, min);
		int maxPos = ROISliceUtils.findPositionOfClosestValueInAxis(x, max);
		
		if (minPos > maxPos) {
			int tmp = maxPos;
			maxPos = minPos;
			minPos = tmp;
		}
		
		return new int[] {minPos, maxPos};
	}

	@Override
	protected String getAppendingName() {
		return "_cropped";
	}

	@Override
	protected Dataset process(Dataset x, Dataset y) {
		// does nothing here
		return null;
	}

}
