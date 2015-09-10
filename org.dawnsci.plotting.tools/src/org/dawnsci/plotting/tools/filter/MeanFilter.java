package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.dawnsci.plotting.tools.ServiceLoader;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;

/**
 * The mean filter.
 * 
 */
public class MeanFilter extends AbstractDelayedFilter {

	@Override
	public int getRank() {
		return 2;
	}

	protected Object[] filter(IDataset data, List<IDataset> axes)
			throws Exception {
		int radius = (int) getConfiguration().get("radius");
		IImageFilterService service = ServiceLoader.getFilter();
		final IDataset mean = service.filterMean(data, radius);
		return new Object[] { mean, axes };
	}

}
