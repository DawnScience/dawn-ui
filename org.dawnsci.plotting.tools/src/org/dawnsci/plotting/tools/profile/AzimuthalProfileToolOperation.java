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
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

public class AzimuthalProfileToolOperation extends AbstractOperation<AzimuthalProfileToolModel, OperationData> {

	@Override
	public String getId() {
		return "org.dawnsci.plotting.tools.profile.AzimuthalProfileToolOperation";
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.ONE;
	}

	@Override
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		final SectorROI sroi = model.getRoi();
		
		IDataset mask = getFirstMask(input);
		
		Dataset[] profile = ROIProfile.sector(DatasetUtils.convertToDataset(input), DatasetUtils.convertToDataset(mask), sroi, false, true, false);
	
		Dataset integral = profile[0];
		integral.setName("azimuthal_profile");
		
		OperationData out = new OperationData(integral);
		
	    if (profile.length>=4 && profile[3]!=null && sroi.hasSeparateRegions()) {
	    	final Dataset reflection = profile[3];
			reflection.setName("azimuthal_sym");
			
			out.setAuxData(reflection);
	    }
		
		return out;
	}

}
