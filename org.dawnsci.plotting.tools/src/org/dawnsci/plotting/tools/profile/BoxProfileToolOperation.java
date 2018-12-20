/*
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

public class BoxProfileToolOperation extends AbstractOperation<BoxProfileToolModel, OperationData> {

	@Override
	public String getId() {
		return "org.dawnsci.plotting.tools.profile.BoxProfileToolOperation";
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
		
		boolean showX = model.isSaveX();
		boolean showY = model.isSaveY();
		RectangularROI roi = model.getRoi();
		
		IDataset mask = getFirstMask(input);
		
		Dataset[] box = showX || showY ? ROIProfile.box(DatasetUtils.convertToDataset(input), DatasetUtils.convertToDataset(mask),
				roi, false) : null;

		List<IDataset> auxData = new ArrayList<>();
		
		if (showX) {
			final Dataset xIntensity = box[0];
			xIntensity.setName("X_Profile");
			auxData.add(xIntensity);
		}

		if (showY) {
			final Dataset yIntensity = box[1];
			yIntensity.setName("Y_Profile");
			auxData.add(yIntensity);
		}
		
		double[] sp = roi.getPoint();
		double[] ep = roi.getEndPoint();

		// Mean, Sum, Std deviation and region
		int xInc = sp[0]<ep[0] ? 1 : -1;
		int yInc = sp[1]<ep[1] ? 1 : -1;

		Dataset dataRegion = DatasetUtils.convertToDataset(input.getSlice(
				new int[] { (int) sp[1], (int) sp[0] },
				new int[] { (int) ep[1],(int) ep[0] },
				new int[] {yInc, xInc}));
		//mean
		Object mean = dataRegion.mean();
		Dataset meands = DatasetFactory.createFromObject(mean, new int[]{1});
		meands.setName("Mean");
		auxData.add(meands);

		//Sum
		Object sum = dataRegion.sum();
		Dataset sumds = DatasetFactory.createFromObject(sum, new int[]{1});
		sumds.setName("Sum");
		auxData.add(sumds);

		//Standard deviation
		Object std = dataRegion.stdDeviation();
		Dataset stds = DatasetFactory.createFromObject(std, new int[]{1});
		stds.setName("Std_Deviation");
		auxData.add(stds);

		dataRegion.setName("Region_Slice");
		
		return new OperationData(dataRegion,(Serializable[])auxData.toArray(new IDataset[auxData.size()]));
	}

}
