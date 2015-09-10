package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Image;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;

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
		int dtype = AbstractDataset.getDType(data);
		Dataset d = DatasetUtils.cast(data, dtype);
		final IDataset pseudoFlatFieldCorrected = Image.backgroundFilter(d, radius);
		return new Object[] { pseudoFlatFieldCorrected, axes };
	}

}
