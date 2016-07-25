package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.dawnsci.plotting.tools.ServiceLoader;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.eclipse.january.dataset.IDataset;

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
		IImageFilterService service = ServiceLoader.getFilter();
		final IDataset gaussianBlur = service.filterGaussianBlur(data, sigma, radius);
		return new Object[] { gaussianBlur, axes };
	}

}
