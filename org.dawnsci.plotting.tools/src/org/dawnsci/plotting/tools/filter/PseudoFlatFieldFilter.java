package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.impl.Image;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

/**
 * Pseudo Flat field filter.
 * 
 */
public class PseudoFlatFieldFilter extends AbstractDelayedFilter {

	@Override
	public int getRank() {
		return 2;
	}

	@Override
	protected Object[] filter(IDataset data, List<IDataset> axes)
			throws Exception {
		int radius = (int) getConfiguration().get("radius");
		Dataset d = DatasetUtils.convertToDataset(data);
		final IDataset pseudoFlatFieldCorrected = Image.pseudoFlatFieldFilter(d, radius);
		return new Object[] { pseudoFlatFieldCorrected, axes };
	}

}
