package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.osgi.services.ServiceProvider;

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
		IImageFilterService service = ServiceProvider.getService(IImageFilterService.class);
		final IDataset eroded = service.filterErode(data, isBinary);
		return new Object[] { eroded, axes };
	}

}
