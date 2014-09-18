package org.dawnsci.plotting.tools.profile;

import java.util.Collection;

import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

public class AzimuthalProfileTool extends SectorProfileTool {
	
	@Override
	protected Dataset[] getXAxis(final SectorROI sroi, Dataset[] integral) {
		
		final Dataset xi;
		
		if (sroi.getSymmetry() != SectorROI.FULL)
			xi = DatasetUtils.linSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(1),integral[0].getSize(), Dataset.FLOAT64);
		else
			xi = DatasetUtils.linSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(0) + 360., integral[0].getSize(), Dataset.FLOAT64);
		xi.setName("Angle (\u00b0)");
		
		if (!sroi.hasSeparateRegions())  return new Dataset[]{xi};
		
		Dataset xii = DatasetUtils.linSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(1), integral[1].getSize(), Dataset.FLOAT64);
		xii.setName("Angle (\u00b0)");
	
		return new Dataset[]{xi, xii};
	}

	@Override
	protected Dataset[] getIntegral(Dataset data,
			                              Dataset mask, 
			                              SectorROI       sroi, 
			                              IRegion         region,
			                              boolean         isDrag,
			                              int             downsample) {


		final Dataset[] profile = ROIProfile.sector(data, mask, sroi, false, true, false);
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
			final Dataset[] profile = ROIProfile.sector((Dataset)slice.getData(), (Dataset)image.getMask(), sroi, false, true, false);
		
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
