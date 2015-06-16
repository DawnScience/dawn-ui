package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.SummedAreaTable;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The fano factor filter. NOTE a 6mil pixel image takes about
 * 800ms on an i7 @see SummedAreaTableTest 
 * 
 * Therefore it may be required to run the filter asynchronously.
 * 
 * @author fcp94556
 *
 */
public class FanoFilter extends AbstractDelayedFilter {

	private static Logger logger = LoggerFactory.getLogger(FanoFilter.class);
	@Override
	public int getRank() {
		return 2;
	}

	protected Object[] filter(IDataset data, List<IDataset> axes) throws Exception {
		
		// TODO Should run in some kind of Job?
		int[]           box   = (int[])getConfiguration().get("box");
		if (box == null) {
			box = new int[]{3,3};
			logger.warn("Unexpected lack of box configuration parameter in "+getClass().getName());
		}
		
        final SummedAreaTable table = new SummedAreaTable((Dataset)data, true);
        final IDataset        fano  = table.getFanoImage(box);
        return new Object[]{fano, axes};
	}

}
