package org.dawnsci.plotting.tools.profile;

import java.util.Collection;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Group;

import org.dawb.common.ui.plot.tools.IDataReductionToolPage.DataReductionInfo;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage.DataReductionSlice;
import org.dawb.gda.extensions.loaders.H5Utils;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.Nexus;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

public class AzimuthalProfileTool extends SectorProfileTool {
	
	@Override
	protected AbstractDataset[] getXAxis(final SectorROI sroi, AbstractDataset[] integral) {
		
		final AbstractDataset xi;
		
		if (sroi.getSymmetry() != SectorROI.FULL)
			xi = DatasetUtils.linSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(1),integral[0].getSize(), AbstractDataset.FLOAT64);
		else
			xi = DatasetUtils.linSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(0) + 360., integral[0].getSize(), AbstractDataset.FLOAT64);
		xi.setName("Angle (°)");
		
		if (!sroi.hasSeparateRegions())  return new AbstractDataset[]{xi};
		
		AbstractDataset xii = DatasetUtils.linSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(1), integral[1].getSize(), AbstractDataset.FLOAT64);
		xii.setName("Angle (°)");
	
		return new AbstractDataset[]{xi, xii};
	}

	@Override
	protected AbstractDataset[] getIntegral(AbstractDataset data,
			                              AbstractDataset mask, 
			                              SectorROI       sroi, 
			                              IRegion         region,
			                              boolean         isDrag) {


		final AbstractDataset[] profile = ROIProfile.sector(data, mask, sroi, false, true, isDrag);
		if (profile==null) return null;
		
		AbstractDataset integral = profile[1];
		integral.setName("Azimuthal Profile "+region.getName());
		

		// If not symmetry profile[3] is null, otherwise plot it.
	    if (profile.length>=4 && profile[3]!=null && sroi.hasSeparateRegions()) {
	    	
			final AbstractDataset reflection = profile[3];
			reflection.setName("Symmetry "+region.getName());

			return new AbstractDataset[]{integral, reflection};
	    	
	    } else {
	    	return new AbstractDataset[]{integral};
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
			final AbstractDataset[] profile = ROIProfile.sector(slice.getData(), image.getMask(), sroi, false, true, false);
		
			AbstractDataset integral = profile[1];
			integral.setName("azimuthal_"+region.getName().replace(' ', '_'));     
			H5Utils.appendDataset(slice.getFile(), slice.getParent(), integral);
			
		    if (profile.length>=4 && profile[3]!=null && sroi.hasSeparateRegions()) {
				final AbstractDataset reflection = profile[3];
				reflection.setName("azimuthal_sym_"+region.getName().replace(' ', '_'));     
				H5Utils.appendDataset(slice.getFile(), slice.getParent(), reflection);
		    }
		}
		return new DataReductionInfo(Status.OK_STATUS);
	}
	

}
