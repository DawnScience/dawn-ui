/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.ServiceLoader;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

public class AzimuthalProfileTool extends SectorProfileTool {
	
	private static final Logger logger = LoggerFactory.getLogger(AzimuthalProfileTool.class);
	
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
			xi = DatasetFactory.createLinearSpace(DoubleDataset.class, sroi.getAngleDegrees(0), sroi.getAngleDegrees(1),integral[0].getSize());
		else
			xi = DatasetFactory.createLinearSpace(DoubleDataset.class, sroi.getAngleDegrees(0), sroi.getAngleDegrees(0) + 360., integral[0].getSize());
		xi.setName("Angle (\u00b0)");
		
		if (!sroi.hasSeparateRegions())  return new Dataset[]{xi};
		
		Dataset xii = DatasetFactory.createLinearSpace(DoubleDataset.class, sroi.getAngleDegrees(0), sroi.getAngleDegrees(1), integral[1].getSize());
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
			slice.appendData(lazyWritables, integral, exportIndex);
			
		    if (profile.length>=4 && profile[3]!=null && sroi.hasSeparateRegions()) {
				final Dataset reflection = profile[3];
				reflection.setName("azimuthal_sym_"+region.getName().replace(' ', '_'));     
				slice.appendData(lazyWritables, reflection, exportIndex);
		    }
		}
		return new DataReductionInfo(Status.OK_STATUS);
	}
	
	protected Action getReductionAction() {
		return new Action("Data reduction...", Activator.getImageDescriptor("icons/run_workflow.gif")) {
			@Override
			public void run() {
				
				try {
					
					final Collection<IRegion> regions = getPlottingSystem().getRegions();
					
					List<IOperation<?, ?>> ops = new ArrayList<>();
					
					for (IRegion region : regions) {
						if (!isRegionTypeSupported(region.getRegionType())) continue;
						
						final SectorROI bounds = (SectorROI)region.getROI();
						if (bounds==null)        continue;
						if (!region.isVisible()) continue;
						
						AzimuthalProfileToolOperation op = (AzimuthalProfileToolOperation)ServiceLoader.getOperationService().create(new AzimuthalProfileToolOperation().getId());
						AzimuthalProfileToolModel model = op.getModel();
						model.setRoi(bounds);
						op.setPassUnmodifiedData(true);
						op.setStoreOutput(true);
						ops.add(op);
					}
					
					if (ops.isEmpty()) {
						//show something
						return;
					}
					
					IOperation<?, ?> last = ops.get(ops.size()-1);
					last.setPassUnmodifiedData(false);
					last.setStoreOutput(false);
					
					ServiceLoader.getOperationUIService().runProcessingWithUI(ops.toArray(new IOperation<?, ?>[ops.size()]), sliceMetadata, null);
				} catch (Exception e) {
					MessageDialog.openError(getSite().getShell(), "Error Reducing Data!", "Could not reduce data! " + e.getMessage());
					logger.error("Could not reduce data!", e);
				}
				
			}
		};
	}
	

}
