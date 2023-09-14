package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * The Gaussian blur filter.
 * 
 */
public class GaussianBlurFilter extends AbstractDelayedFilter {

	@Override
	public int getRank() {
		return 2;
	}

	@Override
	protected Object[] filter(IDataset data, List<IDataset> axes)
			throws Exception {
		int radius = (int) getConfiguration().get("radius");
		double sigma = (double) getConfiguration().get("sigma");
		IImageFilterService service = ServiceProvider.getService(IImageFilterService.class);
		final IDataset gaussianBlur = service.filterGaussianBlur(data, sigma, radius);
		return new Object[] { gaussianBlur, axes };
	}

}
