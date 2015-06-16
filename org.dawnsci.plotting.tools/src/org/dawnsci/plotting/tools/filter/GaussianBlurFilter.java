package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.dawnsci.plotting.tools.ImageFilterServiceLoader;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Gaussian blur filter.
 * 
 */
public class GaussianBlurFilter extends AbstractDelayedFilter {

	private static Logger logger = LoggerFactory.getLogger(GaussianBlurFilter.class);

	@Override
	public int getRank() {
		return 2;
	}

	@Override
	protected Object[] filter(IDataset data, List<IDataset> axes)
			throws Exception {
		int[] box = (int[]) getConfiguration().get("box");
		if (box == null) {
			box = new int[] { 3, 3 };
			logger.warn("Unexpected lack of box configuration parameter in "
					+ getClass().getName());
		}
		double sigma = (double) getConfiguration().get("sigma");
		IImageFilterService service = ImageFilterServiceLoader.getFilter();
		final IDataset gaussianBlur = service.filterGaussianBlur(data, sigma, box[0]);
		return new Object[] { gaussianBlur, axes };
	}

}
