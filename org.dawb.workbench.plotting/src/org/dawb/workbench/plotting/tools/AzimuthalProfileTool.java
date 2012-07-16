package org.dawb.workbench.plotting.tools;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.region.IRegion;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

public class AzimuthalProfileTool extends SectorProfileTool {

	@Override
	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {
        super.configurePlottingSystem(plotter);
        plotter.getSelectedXAxis().setFormatPattern("############.##");
	}
	
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


}
