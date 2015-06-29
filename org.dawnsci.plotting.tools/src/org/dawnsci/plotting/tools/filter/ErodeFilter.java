package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.dawnsci.plotting.tools.ImageFilterServiceLoader;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;

/**
 * The erode filter.
 * 
 */
public class ErodeFilter extends AbstractDelayedFilter {

	@Override
	public int getRank() {
		return 2;
	}

	@Override
	protected Object[] filter(IDataset data, List<IDataset> axes)
			throws Exception {
		boolean isBinary = (boolean)getConfiguration().get("binary");
		IImageFilterService service = ImageFilterServiceLoader.getFilter();
		final IDataset eroded = service.filterErode(data, isBinary);
		return new Object[] { eroded, axes };
	}

}
