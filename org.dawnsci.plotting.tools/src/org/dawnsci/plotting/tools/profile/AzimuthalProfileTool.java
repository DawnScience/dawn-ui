/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.util.Collection;

import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

public class AzimuthalProfileTool extends SectorProfileTool {
	
	
	private IAction metaLock;

	@Override
	protected void configurePlottingSystem(IPlottingSystem<?> plotter) {
		
		this.metaLock = createMetaLock(null);
		
		metaLock.setEnabled(isValidMetadata(getMetaData()));

		getSite().getActionBars().getToolBarManager().add(metaLock);
		getSite().getActionBars().getToolBarManager().add(new Separator());
		getSite().getActionBars().getMenuManager().add(metaLock);
		getSite().getActionBars().getToolBarManager().add(new Separator());

        super.configurePlottingSystem(plotter);
	}
	
	public void activate () {
		super.activate();
		//setup the lock action to work for valid metadata
		checkMetaLock(metaLock);
	}
	
	public void deactivate() {
		super.deactivate();
		unregisterMetadataListeners();
	}

	@Override
	protected Dataset[] getXAxis(final SectorROI sroi, Dataset[] integral) {
		
		final Dataset xi;
		
		if (sroi.getSymmetry() != SectorROI.FULL)
			xi = DatasetFactory.createLinearSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(1),integral[0].getSize(), Dataset.FLOAT64);
		else
			xi = DatasetFactory.createLinearSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(0) + 360., integral[0].getSize(), Dataset.FLOAT64);
		xi.setName("Angle (\u00b0)");
		
		if (!sroi.hasSeparateRegions())  return new Dataset[]{xi};
		
		Dataset xii = DatasetFactory.createLinearSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(1), integral[1].getSize(), Dataset.FLOAT64);
		xii.setName("Angle (\u00b0)");
	
		return new Dataset[]{xi, xii};
	}

	@Override
	protected Dataset[] getIntegral(IDataset data,
			                              IDataset mask, 
			                              SectorROI       sroi, 
			                              IRegion         region,
			                              boolean         isDrag,
			                              int             downsample) {


		final Dataset[] profile = ROIProfile.sector(DatasetUtils.convertToDataset(data), DatasetUtils.convertToDataset(mask), sroi, false, true, false);
		if (profile==null) return null;
		
		Dataset integral = profile[1];
		integral.setName("Azimuthal Profile "+region.getName());
		

		// If not symmetry profile[3] is null, otherwise plot it.
	    if (profile.length>=4 && profile[3]!=null && sroi.hasSeparateRegions()) {
	    	
			final Dataset reflection = profile[3];
			reflection.setName("Symmetry "+region.getName());

			return new Dataset[]{integral, reflection};
	    	
	    } else {
	    	return new Dataset[]{integral, null};
	    }
	}

	@Override
	public DataReductionInfo export(DataReductionSlice slice) throws Exception {
		
		final IImageTrace   image   = getImageTrace();
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		
		for (IRegion region : regions) {
			if (!isRegionTypeSupported(region.getRegionType())) continue;
			if (!region.isVisible())    continue;
			if (!region.isUserRegion()) continue;
			
			final SectorROI sroi = (SectorROI)region.getROI();
			final Dataset[] profile = ROIProfile.sector(DatasetUtils.convertToDataset(slice.getData()), DatasetUtils.convertToDataset(image.getMask()), sroi, false, true, false);
		
			Dataset integral = profile[1];
			integral.setName("azimuthal_"+region.getName().replace(' ', '_'));     
			slice.appendData(integral);
			
		    if (profile.length>=4 && profile[3]!=null && sroi.hasSeparateRegions()) {
				final Dataset reflection = profile[3];
				reflection.setName("azimuthal_sym_"+region.getName().replace(' ', '_'));     
				slice.appendData(reflection);
		    }
		}
		return new DataReductionInfo(Status.OK_STATUS);
	}
	

}
