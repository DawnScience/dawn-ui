package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Image;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pseudo Flat field filter.
 * 
 */
public class PseudoFlatFieldFilter extends AbstractDelayedFilter {

	private static Logger logger = LoggerFactory.getLogger(PseudoFlatFieldFilter.class);

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
		int dtype = AbstractDataset.getDType(data);
		Dataset d = DatasetUtils.cast(data, dtype);
		final IDataset pseudoFlatFieldCorrected = Image.backgroundFilter(d, box[0]);
		return new Object[] { pseudoFlatFieldCorrected, axes };
	}

}
