package org.dawnsci.plotting.tools.processing;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * Tool to normalize an image with given specific parameters
 * @author wqk87977
 *
 */
public class ImageNormalisationProcessTool extends ImageProcessingTool {

	private boolean isDirty = false;
	private AbstractDataset profile;

	public ImageNormalisationProcessTool() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void configureDisplayPlottingSystem(AbstractPlottingSystem plotter) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createProfile(IImageTrace image, IRegion region, IROI roi,
			boolean tryUpdate, boolean isDrag, IProgressMonitor monitor) {
		if(isDirty){
			AbstractDataset ds = ((AbstractDataset)image.getData()).clone();
			AbstractDataset tile = profile.reshape(profile.getShape()[0],1);
			if(roi == null){
				isDirty = false;
				return;
			}
			double width = ((RectangularROI)roi).getLengths()[0];
			tile.idivide(width);
			AbstractDataset correction = DatasetUtils.tile(tile, ds.getShape()[1]);
			ds.idivide(correction);
			
			profilePlottingSystem.updatePlot2D(ds, image.getAxes(), monitor);
			isDirty = false;
		}
		

	}

	@Override
	protected void createDisplayProfile(IImageTrace image, IRegion region,
			IROI roi, boolean tryUpdate, boolean isDrag,
			IProgressMonitor monitor) {
		
		AbstractDataset ds = ((AbstractDataset)image.getData()).clone();
		if(roi == null){
			isDirty = true;
			return;
		}
		AbstractDataset[] profiles = ROIProfile.box(ds, (RectangularROI)roi);
		profile = profiles[1];
		List<IDataset> data = new ArrayList<IDataset>();
		data.add(profiles[1]);
		displayPlottingSystem.clear();
		displayPlottingSystem.createPlot1D(image.getAxes().get(1), data, monitor);
		isDirty = true;
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.XAXIS;
	}
}
