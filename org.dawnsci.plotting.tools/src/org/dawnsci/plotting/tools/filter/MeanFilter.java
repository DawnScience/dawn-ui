package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.dawnsci.plotting.tools.ServiceLoader;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mean filter.
 * 
 */
public class MeanFilter extends AbstractDelayedFilter {

	private static Logger logger = LoggerFactory.getLogger(MeanFilter.class);

	@Override
	public int getRank() {
		return 2;
	}

	protected Object[] filter(IDataset data, List<IDataset> axes)
			throws Exception {
		int[] box = (int[]) getConfiguration().get("box");
		if (box == null) {
			box = new int[] { 3, 3 };
			logger.warn("Unexpected lack of box configuration parameter in "
					+ getClass().getName());
		}
		IImageFilterService service = ServiceLoader.getFilter();
		final IDataset mean = service.filterMean(data, box[0]);
		return new Object[] { mean, axes };
	}

}
